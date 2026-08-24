package ames.comercial.edi2.internal.application.service;

import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.edi2.internal.domain.LiniaTransit;
import ames.comercial.edi2.internal.domain.LiniesDeute;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.edi2.request.MergeComandaRequest;
import ames.comercial.edi2.response.ComandaEDI2Response;
import ames.comercial.edi2.response.ComandaEDI2ResponseImpl;
import ames.comercial.edi2.response.MergeComandaResponse;
import ames.comercial.edi2.response.MergeComandaResponseImpl;
import ames.comercial.entrades.internal.domain.InformacioSortidaEdi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MergeComandaEstrategia3 {

    @Autowired NetejaLiniesTransitService transitService;
    @Autowired DeuteService deuteService;

    public MergeComandaResponse executar(MergeComandaRequest req) {
        // Igual que l'estratègia 0: calculem si hi ha deute o trànsit abans de construir el resultat
        long restant = req.numPecesEndarrerides().orElse(0L) - req.quantitatEnTransit();
        List<LiniaEdi> liniesEdiRestants;
        List<LiniaTransit> transit = new ArrayList<>();
        List<LiniesDeute> deuteList = new ArrayList<>();

        if (restant > 0) {
            // Hi ha deute: saldem les peces endarrerides amb les línies del sistema més antigues
            var result = deuteService.saldarDeute(req.liniesComandaActual(), restant, req.primeraDataEdi(req.isDuesDates()), req.numComanda());
            liniesEdiRestants = req.liniesEdi();
            deuteList = result.deuteList();
        } else {
            // Hi ha trànsit: descomptem les peces en trànsit de les primeres línies EDI
            var result = transitService.aplicarTransit(req.liniesEdi(), restant, req.isDuesDates());
            liniesEdiRestants = result.liniaEdis();
            transit = result.liniesTransit();
        }

        // Unió de totes les dates úniques (sistema + EDI + deute), ordenades cronològicament
        Set<LocalDate> dates = new TreeSet<>();
        dates.addAll(req.liniesComandaActual().stream()
                .map(ObtenirLiniesComandaPendentsResponse::dataSolicitada)
                .collect(Collectors.toSet()));
        dates.addAll(liniesEdiRestants.stream()
                .map(l -> l.dataSortidaAMES(req.isDuesDates()))
                .collect(Collectors.toSet()));
        dates.addAll(deuteList.stream()
                .map(LiniesDeute::dataSolicitada)
                .collect(Collectors.toSet()));

        // Filtrem les dates que superen el límit de dies de tall configurat
        dates = dates.stream()
                .filter(f -> !f.isAfter(req.dataLimitDiesTall()))
                .collect(Collectors.toCollection(TreeSet::new));

        var comanda = buildComanda(dates, req.liniesComandaActual(), liniesEdiRestants,
                req.numComanda(), req.infoSortida(), deuteList);

        return MergeComandaResponseImpl.builder()
                .liniesComanda(comanda)
                .liniesTransit(transit)
                .liniesDeute(deuteList)
                .ultimsAlbarans(req.ultimsAlbarans())
                .albaraReferenciaClient(req.albaraReferenciaClient())
                .isConsiderarUltimsAlbarans(req.isConsiderarUltimsAlbarans())
                .isConsiderarAcumulats(req.isConsiderarAcumulats())
                .build();
    }

    private List<ComandaEDI2Response> buildComanda(Set<LocalDate> dates,
                                                   List<ObtenirLiniesComandaPendentsResponse> liniesComandaActual,
                                                   List<LiniaEdi> aplicarTransitEdi, String numComanda,
                                                   InformacioSortidaEdi infoSortida, List<LiniesDeute> liniesDeute) {
        long acumulatNova = 0L;
        long acumulatActual = 0L;
        List<ComandaEDI2Response> result = new ArrayList<>();

        Map<LocalDate, List<LiniaEdi>> ediPerData = aplicarTransitEdi.stream()
                .filter(e -> e.da().fechaInicial().isPresent())
                .collect(Collectors.groupingBy(e -> e.da().fechaInicial().get()));

        Map<LocalDate, LiniesDeute> deutePerData = liniesDeute.stream()
                .collect(Collectors.toMap(
                        LiniesDeute::dataSolicitada,
                        d -> d,
                        (a, b) -> a));

        List<ObtenirLiniesComandaPendentsResponse> fermes = liniesComandaActual.stream()
                .filter(a -> a.tipus().equals(TipusLiniaComanda.FERM))
                .sorted(Comparator.comparing(ObtenirLiniesComandaPendentsResponse::dataSolicitada))
                .toList();

        List<ObtenirLiniesComandaPendentsResponse> orientatives = liniesComandaActual.stream()
                .filter(a -> a.tipus().equals(TipusLiniaComanda.ORIENTATIU))
                .sorted(Comparator.comparing(ObtenirLiniesComandaPendentsResponse::dataSolicitada))
                .toList();

        // Línies neutres: stock seguretat i inventari, no afecten acumulats
        List<ObtenirLiniesComandaPendentsResponse> neutres = liniesComandaActual.stream()
                .filter(a -> a.tipus().equals(TipusLiniaComanda.STOCK_SEG_AMES)
                        || a.tipus().equals(TipusLiniaComanda.INVENT)
                        || a.tipus().equals(TipusLiniaComanda.STOCK_SEG_CLIENT))
                .toList();

        Map<LocalDate, ObtenirLiniesComandaPendentsResponse> fermesPerData = fermes.stream()
                .collect(Collectors.toMap(
                        ObtenirLiniesComandaPendentsResponse::dataSolicitada,
                        a -> a,
                        (a, b) -> a));

        // 1. Zona FERM + EDI
        for (LocalDate fecha : dates) {
            Optional<ObtenirLiniesComandaPendentsResponse> actual = Optional.ofNullable(fermesPerData.get(fecha));
            List<LiniaEdi> edis = ediPerData.getOrDefault(fecha, List.of());
            Optional<LiniesDeute> deute = Optional.ofNullable(deutePerData.get(fecha));

            if (actual.isPresent()) {
                acumulatActual += actual.get().quantitatPendent();
                result.add(toComandaEdiResponseSistema(actual.get(), 0L, acumulatNova, acumulatActual, false));
            }
            for (LiniaEdi edi : edis) {
                acumulatNova += edi.quantitatNova();
                result.add(toComandaEdiResponseEdi(edi, fecha, numComanda, infoSortida, acumulatNova, acumulatActual));
            }
            // El deute només s'afegeix si no hi ha EDI per aquesta data
            if (deute.isPresent() && edis.isEmpty()) {
                acumulatNova += deute.get().quantitat();
                result.add(toComandaEdiResponseDeute(deute.get(), numComanda, acumulatNova, acumulatActual));
            }
        }

        // 2. Zona ORIENTATIU: compensació de la diferència
        long diferencia = acumulatNova - acumulatActual;

        for (ObtenirLiniesComandaPendentsResponse linia : orientatives) {
            long qty = linia.quantitatSolicitada();
            long consumit = Math.min(qty, diferencia);
            long novaQty = qty - consumit;
            diferencia -= consumit;

            acumulatActual += qty;
            acumulatNova += novaQty;

            result.add(toComandaEdiResponseSistema(linia, novaQty, acumulatNova, acumulatActual, false));
        }

        // 3. Línies neutres (stock seguretat / inventari) → es mostren però no afecten acumulats
        neutres.forEach(linia -> result.add(
                toComandaEdiResponseSistema(linia, linia.quantitatPendent(), 0L, 0L, true)));

        return result;
    }

    // Construeix una línia a partir d'una línia del sistema
    private ComandaEDI2Response toComandaEdiResponseSistema(ObtenirLiniesComandaPendentsResponse actual,
                                                            long novaQty, long acumulatNova, long acumulatActual,
                                                            boolean isNeutre) {
        return ComandaEDI2ResponseImpl.builder()
                .clauLinia(Optional.of(actual.clauLinia()))
                .comandaClient(actual.comandaClient())
                .tipusLiniaComanda(actual.tipus())
                .quantitatActual(actual.quantitatPendent())
                .quantitatNova(novaQty)
                .dataSolicitada(actual.dataSolicitada())
                .dataSortida(actual.dataPrevistaSortida())
                .dataPrevistaSortidaInterna(actual.dataPrevistaSortidaInterna())
                .dataConfirmada(actual.dataConfirmadaFabrica())
                .acumulatNova(acumulatNova)
                .acumulatActual(acumulatActual)
                .isNeutre(isNeutre)
                .observacions("")
                .comentarisInterns(actual.comentarisInterns())
                .comentarisClient(actual.comentarisClient())
                .infoArticle(actual.preu())
                .comandaBlanca(actual.comandaBlanca())
                .empresa(actual.empresa())
                .referencia(actual.referencia())
                .clientNom(actual.clientNom())
                .isPreuFixat(actual.isPreuFixat())
                .build();
    }

    private ComandaEDI2Response toComandaEdiResponseEdi(LiniaEdi edi, LocalDate fecha, String numComanda,
                                                        InformacioSortidaEdi infoSortida,
                                                        long acumulatNova, long acumulatActual) {
        return ComandaEDI2ResponseImpl.builder()
                .clauLinia(Optional.empty())
                .idLinia(edi.idLinia())
                .comandaClient(numComanda)
                .tipusLiniaComanda(TipusLiniaComanda.FERM)
                .quantitatActual(0L)
                .quantitatNova(edi.quantitatNova())
                .dataSolicitada(fecha)
                .dataSortida(infoSortida.calcularDies(fecha))
                .dataPrevistaSortidaInterna(Optional.empty())
                .dataConfirmada(Optional.empty())
                .acumulatNova(acumulatNova)
                .acumulatActual(acumulatActual)
                .isNeutre(false)
                .observacions("")
                .comentarisInterns(edi.comentarisInterns())
                .comentarisClient(edi.comentarisClient())
                .infoArticle(Optional.empty())
                .comandaBlanca(Optional.empty())
                .empresa(Optional.empty())
                .referencia(Optional.empty())
                .clientNom(Optional.empty())
                .isPreuFixat(Optional.empty())
                .build();
    }

    private ComandaEDI2Response toComandaEdiResponseDeute(LiniesDeute deute, String numComanda,
                                                          long acumulatNova, long acumulatActual) {
        return ComandaEDI2ResponseImpl.builder()
                .clauLinia(Optional.empty())
                .comandaClient(numComanda)
                .tipusLiniaComanda(TipusLiniaComanda.FERM)
                .quantitatActual(0L)
                .quantitatNova(deute.quantitat())
                .dataSolicitada(deute.dataSolicitada())
                .dataSortida(deute.dataSolicitada())
                .dataPrevistaSortidaInterna(Optional.empty())
                .dataConfirmada(Optional.empty())
                .acumulatNova(acumulatNova)
                .acumulatActual(acumulatActual)
                .isNeutre(false)
                .observacions("")
                .infoArticle(Optional.empty())
                .comandaBlanca(Optional.empty())
                .empresa(Optional.empty())
                .referencia(Optional.empty())
                .clientNom(Optional.empty())
                .isPreuFixat(Optional.empty())
                .build();
    }
}
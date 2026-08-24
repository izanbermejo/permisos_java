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
public class MergeComandaEstrategia4 {

    @Autowired NetejaLiniesTransitService transitService;
    @Autowired DeuteService deuteService;
    @Autowired NetejaLiniesEdiByFermService netejaLiniesEdiByFermService;

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

        // Calculem la quantitat total de les FERMs del sistema.
        // Aquesta quantitat s'usarà per netejar les primeres línies EDI:
        // les FERMs ja estan processades al sistema, per tant no cal mostrar-les a l'EDI.
        long totalFermes = req.liniesComandaActual().stream()
                .filter(a -> a.tipus().equals(TipusLiniaComanda.FERM))
                .mapToLong(ObtenirLiniesComandaPendentsResponse::quantitatPendent)
                .sum();

        // Eliminem de les línies EDI les quantitats ja cobertes per les FERMs del sistema.
        // Les línies EDI totalment consumides desapareixen; les parcialment consumides es redueixen.
        List<LiniaEdi> liniesEdiNetes = netejaLiniesEdiByFermService.netejar(liniesEdiRestants, totalFermes);

        // Unió de totes les dates úniques (sistema + EDI net + deute), ordenades cronològicament
        Set<LocalDate> dates = new TreeSet<>();
        dates.addAll(req.liniesComandaActual().stream()
                .map(ObtenirLiniesComandaPendentsResponse::dataSolicitada)
                .collect(Collectors.toSet()));
        dates.addAll(liniesEdiNetes.stream()
                .map(l -> l.dataSortidaAMES(req.isDuesDates()))
                .collect(Collectors.toSet()));
        dates.addAll(deuteList.stream()
                .map(LiniesDeute::dataSolicitada)
                .collect(Collectors.toSet()));

        // Filtrem les dates que superen el límit de dies de tall configurat
        dates = dates.stream()
                .filter(f -> !f.isAfter(req.dataLimitDiesTall()))
                .collect(Collectors.toCollection(TreeSet::new));

        var comanda = buildComanda(dates, req.liniesComandaActual(), liniesEdiNetes,
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
                                                   List<LiniaEdi> liniesEdiNetes, String numComanda,
                                                   InformacioSortidaEdi infoSortida, List<LiniesDeute> liniesDeute) {
        long acumulatNova = 0L;
        long acumulatActual = 0L;
        List<ComandaEDI2Response> result = new ArrayList<>();

        // Pre-indexem per data per fer lookups O(1) al bucle
        Map<LocalDate, ObtenirLiniesComandaPendentsResponse> actualPerData = liniesComandaActual.stream()
                .collect(Collectors.toMap(
                        ObtenirLiniesComandaPendentsResponse::dataSolicitada,
                        a -> a,
                        (a, b) -> a)); // si hi ha duplicats de data, agafem el primer

        Map<LocalDate, List<LiniaEdi>> ediPerData = liniesEdiNetes.stream()
                .filter(e -> e.da().fechaInicial().isPresent())
                .collect(Collectors.groupingBy(e -> e.da().fechaInicial().get()));

        Map<LocalDate, LiniesDeute> deutePerData = liniesDeute.stream()
                .collect(Collectors.toMap(
                        LiniesDeute::dataSolicitada,
                        d -> d,
                        (a, b) -> a)); // si hi ha duplicats de data, agafem el primer

        for (LocalDate fecha : dates) {
            Optional<ObtenirLiniesComandaPendentsResponse> actual = Optional.ofNullable(actualPerData.get(fecha));
            List<LiniaEdi> edis = ediPerData.getOrDefault(fecha, List.of());
            Optional<LiniesDeute> deute = Optional.ofNullable(deutePerData.get(fecha));

            if (actual.isEmpty() && edis.isEmpty() && deute.isEmpty()) continue;
            if (actual.isPresent() && actual.get().quantitatPendent() == 0 && edis.isEmpty() && deute.isEmpty()) continue;

            /*
             * Sistema + EDI en la misma fecha:
             * La línea del sistema absorbe el EDI.
             */
            if (actual.isPresent() && !edis.isEmpty()) {
                LiniaEdi edi = edis.get(0);
                long qtyActual = actual.get().quantitatPendent();
                long qtyNova = edi.quantitatNova();
                acumulatActual += qtyActual;
                acumulatNova += qtyNova;
                result.add(ComandaEDI2ResponseImpl.builder()
                        .clauLinia(Optional.of(actual.get().clauLinia()))
                        .comandaClient(actual.get().comandaClient())
                        .idLinia(Optional.of(edi.idLinia()))
                        .tipusLiniaComanda(actual.get().tipus())
                        .quantitatActual(qtyActual)
                        .quantitatNova(qtyNova)
                        .dataSolicitada(fecha)
                        .dataSortida(infoSortida.calcularDies(fecha))
                        .dataPrevistaSortidaInterna(actual.get().dataPrevistaSortidaInterna())
                        .dataConfirmada(actual.get().dataConfirmadaFabrica())
                        .acumulatNova(acumulatNova)
                        .acumulatActual(acumulatActual)
                        .isNeutre(false)
                        .observacions("")
                        .comentarisInterns(edi.comentarisInterns())
                        .comentarisClient(edi.comentarisClient())
                        .infoArticle(actual.map(ObtenirLiniesComandaPendentsResponse::preu))
                        .comandaBlanca(actual.flatMap(ObtenirLiniesComandaPendentsResponse::comandaBlanca))
                        .empresa(actual.map(ObtenirLiniesComandaPendentsResponse::empresa))
                        .referencia(actual.map(ObtenirLiniesComandaPendentsResponse::referencia))
                        .clientNom(actual.map(ObtenirLiniesComandaPendentsResponse::clientNom))
                        .isPreuFixat(actual.map(ObtenirLiniesComandaPendentsResponse::isPreuFixat))
                        .build());

                continue;
            }
            /*
             * Solo sistema
             */
            if (actual.isPresent()) {
                boolean tipusNeutre = actual.get().tipus().equals(TipusLiniaComanda.STOCK_SEG_AMES)
                        || actual.get().tipus().equals(TipusLiniaComanda.INVENT)
                        || actual.get().tipus().equals(TipusLiniaComanda.STOCK_SEG_CLIENT);
                long qty = actual.get().quantitatPendent();
                acumulatActual += qty;
                if (!tipusNeutre) {
                    acumulatNova += qty;
                }
                result.add(ComandaEDI2ResponseImpl.builder()
                        .clauLinia(Optional.of(actual.get().clauLinia()))
                        .comandaClient(actual.get().comandaClient())
                        .idLinia(Optional.empty())
                        .tipusLiniaComanda(actual.get().tipus())
                        .quantitatActual(qty)
                        .quantitatNova(tipusNeutre ? qty : qty)
                        .dataSolicitada(fecha)
                        .dataSortida(actual.get().dataPrevistaSortida())
                        .dataPrevistaSortidaInterna(actual.get().dataPrevistaSortidaInterna())
                        .dataConfirmada(actual.get().dataConfirmadaFabrica())
                        .acumulatNova(acumulatNova)
                        .acumulatActual(acumulatActual)
                        .isNeutre(tipusNeutre)
                        .observacions("")
                        .comentarisInterns(actual.get().comentarisInterns())
                        .comentarisClient(actual.get().comentarisClient())
                        .infoArticle(actual.map(ObtenirLiniesComandaPendentsResponse::preu))
                        .comandaBlanca(actual.flatMap(ObtenirLiniesComandaPendentsResponse::comandaBlanca))
                        .empresa(actual.map(ObtenirLiniesComandaPendentsResponse::empresa))
                        .referencia(actual.map(ObtenirLiniesComandaPendentsResponse::referencia))
                        .clientNom(actual.map(ObtenirLiniesComandaPendentsResponse::clientNom))
                        .isPreuFixat(actual.map(ObtenirLiniesComandaPendentsResponse::isPreuFixat))
                        .build());
            }
            /*
             * Solo EDI
             */
            for (LiniaEdi edi : edis) {
                acumulatNova += edi.quantitatNova();
                result.add(ComandaEDI2ResponseImpl.builder()
                        .clauLinia(Optional.empty())
                        .comandaClient(numComanda)
                        .idLinia(Optional.of(edi.idLinia()))
                        .tipusLiniaComanda(TipusLiniaComanda.ORIENTATIU)
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
                        .build());
            }
            /*
             * Deute sin EDI
             */
            if (deute.isPresent() && edis.isEmpty()) {
                acumulatNova += deute.get().quantitat();
                result.add(ComandaEDI2ResponseImpl.builder()
                        .clauLinia(Optional.empty())
                        .comandaClient(numComanda)
                        .idLinia(Optional.empty())
                        .tipusLiniaComanda(TipusLiniaComanda.ORIENTATIU)
                        .quantitatActual(0L)
                        .quantitatNova(deute.get().quantitat())
                        .dataSolicitada(fecha)
                        .dataSortida(fecha)
                        .dataPrevistaSortidaInterna(Optional.empty())
                        .dataConfirmada(Optional.empty())
                        .acumulatNova(acumulatNova)
                        .acumulatActual(acumulatActual)
                        .isNeutre(false)
                        .observacions("")
                        .comentarisInterns(Optional.empty())
                        .comentarisClient(Optional.empty())
                        .infoArticle(Optional.empty())
                        .comandaBlanca(Optional.empty())
                        .empresa(Optional.empty())
                        .referencia(Optional.empty())
                        .clientNom(Optional.empty())
                        .isPreuFixat(Optional.empty())
                        .build());
            }
        }

        // Ordenem el resultat final per data sol·licitada
        result.sort(Comparator.comparing(ComandaEDI2Response::dataSortida));
        return result;
    }
}
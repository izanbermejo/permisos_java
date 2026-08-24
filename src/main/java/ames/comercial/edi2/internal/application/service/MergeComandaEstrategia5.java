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
public class MergeComandaEstrategia5 {

    @Autowired NetejaLiniesEdiByFermService netejaLiniesEdiByFermService;

    public MergeComandaResponse executar(MergeComandaRequest req) {
        List<LiniaTransit> transit = new ArrayList<>();
        List<LiniesDeute> deuteList = new ArrayList<>();

        // Descomptem de les línies EDI la quantitat total de les FERMs del sistema
        /*long totalFermes = req.liniesComandaActual().stream()
                .filter(a -> a.tipus().equals(TipusLiniaComanda.FERM))
                .mapToLong(ObtenirLiniesComandaPendentsResponse::quantitatPendent)
                .sum();

        List<LiniaEdi> liniesEdiNetes = netejaLiniesEdiByFermService.netejar(req.liniesEdi(), totalFermes);*/

        // Unió de totes les dates úniques
        Set<LocalDate> dates = new TreeSet<>();
        dates.addAll(req.liniesComandaActual().stream()
                .map(ObtenirLiniesComandaPendentsResponse::dataSolicitada)
                .collect(Collectors.toSet()));
        dates.addAll(req.liniesEdi().stream()
                .map(l -> l.dataSortidaAMES(req.isDuesDates()))
                .collect(Collectors.toSet()));

        dates = dates.stream()
                .filter(f -> !f.isAfter(req.dataLimitDiesTall()))
                .collect(Collectors.toCollection(TreeSet::new));

        var comanda = buildComanda(dates, req.liniesComandaActual(), req.liniesEdi(),
                req.numComanda(), req.infoSortida());

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
                                                   InformacioSortidaEdi infoSortida) {
        long acumulatNova = 0L;
        long acumulatActual = 0L;
        List<ComandaEDI2Response> result = new ArrayList<>();

        boolean teTerceraData = liniesComandaActual.stream()
                .filter(l -> l.comandaClient().equals(numComanda))
                .anyMatch(l -> l.dataPrevistaSortidaInterna().isPresent());

        // Pre-indexem per data
        Map<LocalDate, ObtenirLiniesComandaPendentsResponse> actualPerData = liniesComandaActual.stream()
                .collect(Collectors.toMap(
                        ObtenirLiniesComandaPendentsResponse::dataSolicitada,
                        a -> a,
                        (a, b) -> a));

        Map<LocalDate, List<LiniaEdi>> ediPerData = liniesEdiNetes.stream()
                .filter(e -> e.da().fechaInicial().isPresent())
                .collect(Collectors.groupingBy(e -> e.da().fechaInicial().get()));

        for (LocalDate fecha : dates) {
            Optional<ObtenirLiniesComandaPendentsResponse> actual = Optional.ofNullable(actualPerData.get(fecha));
            List<LiniaEdi> edis = ediPerData.getOrDefault(fecha, List.of());

            if (actual.isEmpty() && edis.isEmpty()) continue;
            if (actual.isPresent() && actual.get().quantitatPendent() == 0 && edis.isEmpty()) continue;

            long qty = actual.map(ObtenirLiniesComandaPendentsResponse::quantitatPendent).orElse(0L);

            if (actual.isPresent()) {
                boolean esOrientatiu = actual.get().tipus().equals(TipusLiniaComanda.ORIENTATIU);
                // Fusionem només si hi ha exactament un EDI, és ORIENTATIU i és la mateixa comanda
                boolean fusionar = esOrientatiu && edis.size() == 1 && actual.get().comandaClient().equals(numComanda);

                boolean tipusNeutre = actual.get().tipus().equals(TipusLiniaComanda.STOCK_SEG_AMES)
                        || actual.get().tipus().equals(TipusLiniaComanda.INVENT)
                        || actual.get().tipus().equals(TipusLiniaComanda.STOCK_SEG_CLIENT);

                if (!tipusNeutre) acumulatActual += qty;

                if (fusionar) {
                    LiniaEdi ediUnic = edis.get(0);
                    long novaQty = ediUnic.quantitatNova();
                    if (!tipusNeutre) acumulatNova += novaQty;
                    result.add(ComandaEDI2ResponseImpl.builder()
                            .clauLinia(Optional.of(actual.get().clauLinia()))
                            .idLinia(Optional.of(ediUnic.idLinia()))
                            .comandaClient(actual.get().comandaClient())
                            .tipusLiniaComanda(actual.get().tipus())
                            .quantitatActual(qty)
                            .quantitatNova(novaQty)
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
                } else {
                    if (!tipusNeutre) acumulatNova += !esOrientatiu ? qty : 0L;
                    result.add(ComandaEDI2ResponseImpl.builder()
                            .clauLinia(Optional.of(actual.get().clauLinia()))
                            .idLinia(Optional.empty())
                            .comandaClient(actual.get().comandaClient())
                            .tipusLiniaComanda(actual.get().tipus())
                            .quantitatActual(qty)
                            .quantitatNova(!esOrientatiu ? qty : 0L)
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
            }

            // EDIs que no s'han fusionat → van per separat
            boolean fusionat = actual.isPresent()
                    && actual.get().tipus().equals(TipusLiniaComanda.ORIENTATIU)
                    && actual.get().comandaClient().equals(numComanda)
                    && edis.size() == 1;

            List<LiniaEdi> edisPerSeparat = fusionat ? List.of() : edis;

            for (LiniaEdi edi : edisPerSeparat) {
                acumulatNova += edi.quantitatNova();
                result.add(ComandaEDI2ResponseImpl.builder()
                        .clauLinia(Optional.empty())
                        .idLinia(Optional.of(edi.idLinia()))
                        .comandaClient(numComanda)
                        .tipusLiniaComanda(TipusLiniaComanda.ORIENTATIU)
                        .quantitatActual(0L)
                        .quantitatNova(edi.quantitatNova())
                        .dataSolicitada(fecha)
                        .dataSortida(infoSortida.calcularDies(fecha))
                        .dataPrevistaSortidaInterna(
                                teTerceraData
                                        ? Optional.of(infoSortida.calcularDies(fecha))
                                        : Optional.empty()
                        )
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
        }

        result.sort(Comparator.comparing(ComandaEDI2Response::dataSortida));
        return result;
    }
}
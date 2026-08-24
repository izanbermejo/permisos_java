package ames.comercial.edi2.internal.application.service;

import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.edi2.request.MergeComandaRequest;
import ames.comercial.edi2.response.ComandaEDI2Response;
import ames.comercial.edi2.response.ComandaEDI2ResponseImpl;
import ames.comercial.edi2.response.MergeComandaResponse;
import ames.comercial.edi2.response.MergeComandaResponseImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class MergeComandaEstrategia2 {

    public MergeComandaResponse executar(MergeComandaRequest req) {

        // Aquesta estratègia no gestiona trànsit ni deute.
        // El client envia una quantitat global que cal descomptar de les línies del sistema en ordre de data.

        // Suma total de totes les quantitats que vénen per EDI
        long ediTotal = req.liniesEdi().stream()
                .mapToLong(LiniaEdi::quantitatNova)
                .sum();

        // Data de la línia EDI: sempre agafem la més pròxima com a referència
        // Data sol·licitada → dataSolicitada() (fechaFinal si existeix, sinó fechaInicial)
        LocalDate dataEdiSolicitada = req.liniesEdi().stream()
                .map(l -> l.dataSolicitada(req.isDuesDates()))
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(LocalDate.now());

        // Data sortida AMES → dataSortidaAMES() (fechaInicial si existeix, sinó fechaFinal)
        LocalDate dataEdiSortida = req.liniesEdi().stream()
                .map(l -> l.dataSortidaAMES(req.isDuesDates()))
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .orElse(LocalDate.now());

        List<ComandaEDI2Response> result = new ArrayList<>();
        // restant és la quantitat EDI que encara no hem consumit de les línies del sistema
        long restant = ediTotal;

        boolean teTerceraData = req.liniesComandaActual().stream()
                .anyMatch(l -> l.dataPrevistaSortidaInterna().isPresent());

        Optional<LocalDate> dataPrevistaSortidaInternaEdi = teTerceraData
                ? Optional.of(req.infoSortida().calcularDies(dataEdiSolicitada))
                : Optional.empty();
        // Línia compensadora: representa la quantitat total que el client ha demanat per EDI.
        // Els acumulats es posen a 0 perquè es recalcularan al final, un cop ordenat per data.
        result.add(ComandaEDI2ResponseImpl.builder()
                .clauLinia(Optional.empty())
                .comandaClient(req.numeroComandaEDI().orElse(req.numComanda()))
                .idLinia(req.liniesEdi().get(0).idLinia())
                .tipusLiniaComanda(TipusLiniaComanda.FERM)
                .quantitatActual(0L)
                .quantitatNova(ediTotal)
                .dataSolicitada(dataEdiSolicitada)
                .dataSortida(dataEdiSortida)
                .dataPrevistaSortidaInterna(dataPrevistaSortidaInternaEdi)
                .dataConfirmada(Optional.empty())
                .acumulatNova(0L)     // es recalcularà després
                .acumulatActual(0L)   // es recalcularà després
                .isNeutre(false)
                .observacions("")
                .comentarisInterns(req.liniesEdi().get(0).comentarisInterns())
                .comentarisClient(req.liniesEdi().get(0).comentarisClient())
                .infoArticle(Optional.empty())
                .comandaBlanca(Optional.empty())
                .empresa(Optional.empty())
                .referencia(Optional.empty())
                .clientNom(Optional.empty())
                .isPreuFixat(Optional.empty())
                .build());

        // Recorrem totes les línies del sistema ordenades per data.
        // Les anem consumint amb el total EDI fins que restant = 0.
        List<ObtenirLiniesComandaPendentsResponse> liniesOrdenades = req.liniesComandaActual().stream()
                .sorted(Comparator.comparing(ObtenirLiniesComandaPendentsResponse::dataSolicitada))
                .toList();

        for (ObtenirLiniesComandaPendentsResponse linia : liniesOrdenades) {
            boolean tipusNeutre = linia.tipus().equals(TipusLiniaComanda.STOCK_SEG_AMES)
                    || linia.tipus().equals(TipusLiniaComanda.INVENT)
                    || linia.tipus().equals(TipusLiniaComanda.STOCK_SEG_CLIENT);

            long qty = linia.quantitatPendent();
            if (qty == 0) continue;

            // La dataSortida canvia segons si el client usa dues dates o no
            LocalDate dataSortida = req.considerarDuesDates()
                    ? req.infoSortida().calcularDies(linia.dataPrevistaSortida())
                    : linia.dataPrevistaSortida(); // data del sistema tal qual, sense recalcular

            // Les FERMs no es toquen → novaQty = qty
            if (!linia.tipus().equals(TipusLiniaComanda.ORIENTATIU)) {
                result.add(ComandaEDI2ResponseImpl.builder()
                        .clauLinia(Optional.of(linia.clauLinia()))
                        .comandaClient(linia.comandaClient())
                        .idLinia(Optional.empty())
                        .tipusLiniaComanda(linia.tipus())
                        .quantitatActual(qty)
                        .quantitatNova(qty)
                        .dataSolicitada(linia.dataSolicitada())
                        .dataSortida(dataSortida)
                        .dataPrevistaSortidaInterna(linia.dataPrevistaSortidaInterna())
                        .dataConfirmada(linia.dataConfirmadaFabrica())
                        .acumulatNova(0L)
                        .acumulatActual(0L)
                        .isNeutre(tipusNeutre)
                        .observacions("")
                        .comentarisInterns(linia.comentarisInterns())
                        .comentarisClient(linia.comentarisClient())
                        .infoArticle(linia.preu())
                        .comandaBlanca(linia.comandaBlanca())
                        .empresa(linia.empresa())
                        .referencia(linia.referencia())
                        .clientNom(linia.clientNom())
                        .isPreuFixat(linia.isPreuFixat())
                        .build());
                continue;
            }

            // Les ORIENTATIU es van consumint amb el restant EDI
            long consumit = Math.min(qty, restant);
            long novaQty = qty - consumit;
            restant -= consumit;

            result.add(ComandaEDI2ResponseImpl.builder()
                    .clauLinia(Optional.of(linia.clauLinia()))
                    .comandaClient(linia.comandaClient())
                    .idLinia(Optional.empty())
                    .tipusLiniaComanda(linia.tipus())
                    .quantitatActual(qty)
                    .quantitatNova(novaQty)
                    .dataSolicitada(linia.dataSolicitada())
                    .dataSortida(dataSortida)
                    .dataPrevistaSortidaInterna(linia.dataPrevistaSortidaInterna())
                    .dataConfirmada(linia.dataConfirmadaFabrica())
                    .acumulatNova(0L)
                    .acumulatActual(0L)
                    .isNeutre(tipusNeutre)
                    .observacions("")
                    .comentarisInterns(linia.comentarisInterns())
                    .comentarisClient(linia.comentarisClient())
                    .infoArticle(linia.preu())
                    .comandaBlanca(linia.comandaBlanca())
                    .empresa(linia.empresa())
                    .referencia(linia.referencia())
                    .clientNom(linia.clientNom())
                    .isPreuFixat(linia.isPreuFixat())
                    .build());
        }

        // Ordenem per data i recalculem els acumulats en un sol pas.
        // Cal fer-ho al final perquè la línia compensadora pot quedar en qualsevol posició
        // depenent de la seva data respecte a les línies del sistema.
        result.sort(Comparator.comparing(ComandaEDI2Response::dataSolicitada));

        long acNova = 0L;
        long acActual = 0L;
        List<ComandaEDI2Response> resultFinal = new ArrayList<>();
        for (ComandaEDI2Response linia : result) {
            acActual += linia.quantitatActual();
            acNova += linia.quantitatNova();
            resultFinal.add(ComandaEDI2ResponseImpl.builder()
                    .from(linia)
                    .acumulatActual(acActual)
                    .acumulatNova(acNova)
                    .build());
        }

        return MergeComandaResponseImpl.builder()
                .liniesComanda(resultFinal)
                .liniesTransit(List.of())
                .liniesDeute(List.of())
                .ultimsAlbarans(req.ultimsAlbarans())
                .albaraReferenciaClient(req.albaraReferenciaClient())
                .isConsiderarUltimsAlbarans(req.isConsiderarUltimsAlbarans())
                .isConsiderarAcumulats(req.isConsiderarAcumulats())
                .build();
    }
}
package ames.comercial.edi2.internal.application.service;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.advantage.internal.response.ArticleClientInformacioComandaResponse;
import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.edi2.internal.domain.LiniaMerge;
import ames.comercial.edi2.internal.domain.LiniaMergeImpl;
import ames.comercial.edi2.internal.domain.LiniaTransit;
import ames.comercial.edi2.internal.domain.LiniesDeute;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.edi2.request.MergeComandaRequest;
import ames.comercial.edi2.response.ComandaEDI2Response;
import ames.comercial.edi2.response.ComandaEDI2ResponseImpl;
import ames.comercial.edi2.response.MergeComandaResponse;
import ames.comercial.edi2.response.MergeComandaResponseImpl;
import ames.comercial.entrades.internal.domain.InformacioSortidaEdi;
import ames.comercial.inventari.ext.IObtenirStocks;
import ames.comercial.shared.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MergeComandaEstrategia1 {

    @Autowired NetejaLiniesTransitService transitService;
    @Autowired DeuteService deuteService;
    @Autowired IObtenirStocks iObtenirStocks;
    @Autowired IObtenirArticleClientInformacioComanda obtenirInfoArtCli;

    public MergeComandaResponse executar(MergeComandaRequest req) {
        // Igual que l'estratègia 0: calculem si hi ha deute o trànsit abans de construir el resultat
        long restant = req.numPecesEndarrerides().orElse(0L) - req.quantitatEnTransit();
        List<LiniaEdi> liniesEdiRestants;
        List<LiniaTransit> transit = new ArrayList<>();
        List<LiniesDeute> deuteList = new ArrayList<>();

        // Primera data que ve per EDI → a partir d'aquí usem quantitats EDI.
        // Les dates anteriors a aquesta es mostren amb quantitatNova = 0.
        LocalDate primeraDataEdi = req.liniesEdi().stream()
                .map(l -> l.dataSortidaAMES(req.isDuesDates()))
                .min(Comparator.naturalOrder())
                .orElse(LocalDate.MAX);

        if (restant > 0) {
            // Hi ha deute: saldem les peces endarrerides amb les línies del sistema més antigues
            var result = deuteService.saldarDeute(req.liniesComandaActual(), restant, req.primeraDataEdi(req.isDuesDates()), req.numComanda());
            liniesEdiRestants = result.liniaEdis();
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

        List<LiniaMerge> linies = buildLinies(
                dates,
                liniesEdiRestants,
                deuteList,
                primeraDataEdi,
                req,
                transit
        );

        List<ComandaEDI2Response> comanda = new ArrayList<>();

        long acumulatNova = 0;
        long acumulatActual = 0;

        for (LiniaMerge linia : linies) {
            acumulatNova += linia.quantitatNova();
            acumulatActual += linia.actual()
                    .map(ObtenirLiniesComandaPendentsResponse::quantitatPendent)
                    .orElse(0L);
            comanda.add(toComandaEdiResponse(linia, req.numComanda(), req.infoSortida(), acumulatNova, acumulatActual));
        }

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

    private List<LiniaMerge> buildLinies(Set<LocalDate> dates, List<LiniaEdi> edis, List<LiniesDeute> deutes,
                                         LocalDate primeraDataEdi, MergeComandaRequest request, List<LiniaTransit> transit) {

        List<LiniaMerge> resultat = new ArrayList<>();

        var magatzem = obtenirInfoArtCli.executar(request.comanda().articleClient())
                .map(ArticleClientInformacioComandaResponse::magatzemSortida)
                .orElse("");

        var quantitatSistemaAnterior = request.empresa()
                .flatMap(empresa -> iObtenirStocks.query(
                        request.comanda().articleClient(),
                        empresa,
                        magatzem
                ))
                .orElse(Stock.empty());

        long quantitatTransit = transit.stream()
                .mapToLong(LiniaTransit::quantitat)
                .sum();

        //Per tal de que no surti una linia artificial negativa, posem que el valor maxim sigui 0
        long quantitatArtificial = Math.max(0, quantitatSistemaAnterior.stock() - quantitatTransit);

        /*
         * Les línies antigues mantenen quantitat actual,
         * però la nova passa a 0
         */
        request.liniesComandaActual().stream()
                .filter(a -> a.dataSolicitada().isBefore(primeraDataEdi))
                .forEach(a ->
                        resultat.add(LiniaMergeImpl.builder()
                                .data(a.dataSolicitada())
                                .actual(Optional.of(a))
                                .edi(Optional.empty())
                                .deute(Optional.empty())
                                .quantitatNova(0)
                                .artificial(false)
                                .build())
                );

        /*
         * Creació línia artificial
         */
        if (quantitatArtificial > 0) {
            LocalDate dataArtificial = LocalDate.now();
            if (!dataArtificial.isBefore(primeraDataEdi)) {
                dataArtificial = request.infoSortida().calcularDies(primeraDataEdi.minusDays(1));
            }

            resultat.add(LiniaMergeImpl.builder()
                    .data(dataArtificial)
                    .actual(Optional.empty())
                    .edi(Optional.empty())
                    .deute(Optional.empty())
                    .quantitatNova(quantitatArtificial)
                    .artificial(false)
                    .build());
        }

        for(LocalDate data : dates){

            if(data.isBefore(primeraDataEdi)) continue;

            Optional<ObtenirLiniesComandaPendentsResponse> actual = request.liniesComandaActual().stream()
                    .filter(a -> a.dataSolicitada().equals(data))
                    .findFirst();

            Optional<LiniaEdi> edi = edis.stream()
                    .filter(e -> e.dataSortidaAMES(request.isDuesDates()).equals(data))
                    .findFirst();

            Optional<LiniesDeute> deute = deutes.stream()
                    .filter(d -> d.dataSolicitada().equals(data))
                    .findFirst();

            resultat.add(LiniaMergeImpl.builder()
                    .data(data)
                    .actual(actual)
                    .edi(edi)
                    .deute(deute)
                    .quantitatNova(
                            edi.map(LiniaEdi::quantitatNova)
                                    .orElse(deute.map(LiniesDeute::quantitat)
                                            .orElse(0L))
                    )
                    .artificial(false)
                    .build());
        }

        return resultat.stream()
                .sorted(Comparator.comparing(LiniaMerge::data))
                .toList();
    }

    private ComandaEDI2Response toComandaEdiResponse(LiniaMerge linia, String numComanda, InformacioSortidaEdi infoSortida,
                                                     long acumulatNova, long acumulatActual) {

        return ComandaEDI2ResponseImpl.builder()
                .clauLinia(linia.actual().map(ObtenirLiniesComandaPendentsResponse::clauLinia))
                .comandaClient(numComanda)
                .idLinia(linia.edi().map(LiniaEdi::idLinia))
                .tipusLiniaComanda(
                        linia.actual()
                                .map(ObtenirLiniesComandaPendentsResponse::tipus)
                                .orElse(TipusLiniaComanda.FERM)
                )
                .quantitatActual(
                        linia.actual()
                                .map(ObtenirLiniesComandaPendentsResponse::quantitatPendent)
                                .orElse(0L)
                )
                .quantitatNova(linia.quantitatNova())
                .dataSolicitada(linia.data())
                .dataSortida(
                        linia.edi().isPresent()
                                ? infoSortida.calcularDies(linia.data())
                                : linia.actual()
                                .map(ObtenirLiniesComandaPendentsResponse::dataPrevistaSortida)
                                .orElse(infoSortida.calcularDies(LocalDate.now()))
                )
                .dataPrevistaSortidaInterna(
                        linia.actual().flatMap(
                                ObtenirLiniesComandaPendentsResponse::dataPrevistaSortidaInterna
                        ).orElse(
                                infoSortida.calcularDies(linia.data()
                                )
                        )
                )
                .acumulatNova(acumulatNova)
                .acumulatActual(acumulatActual)
                .isNeutre(linia.artificial())
                .observacions("")
                .build();
    }

}
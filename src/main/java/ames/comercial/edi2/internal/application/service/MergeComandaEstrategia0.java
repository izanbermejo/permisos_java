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
public class MergeComandaEstrategia0 {

    @Autowired NetejaLiniesTransitService transitService;
    @Autowired DeuteService deuteService;

    public MergeComandaResponse executar(MergeComandaRequest req) {
        // Calculem si hi ha peces endarrerides (deute) o en trànsit.
        // Si restant > 0 → hi ha deute pendent de saldar
        // Si restant <= 0 → hi ha peces en trànsit que cal descomptar de l'EDI
        long restant = req.numPecesEndarrerides().orElse(0L) - req.quantitatEnTransit();
        List<LiniaEdi> liniesEdiRestants;
        List<LiniaTransit> transit = new ArrayList<>();
        List<LiniesDeute> deuteList = new ArrayList<>();

        if (restant > 0) {
            // Hi ha deute: saldem les peces endarrerides amb les línies del sistema més antigues.
            // Les línies EDI es mantenen intactes; el deute es mostra per separat.
            var result = deuteService.saldarDeute(req.liniesComandaActual(), restant, req.primeraDataEdi(req.isDuesDates()), req.numComanda());
            liniesEdiRestants = req.liniesEdi();
            deuteList = result.deuteList();
        } else {
            // Hi ha trànsit: descomptem les peces en trànsit de les primeres línies EDI.
            // Les línies consumides passen a la llista de trànsit i no es mostren al resultat principal.
            var result = transitService.aplicarTransit(req.liniesEdi(), restant, req.isDuesDates());
            liniesEdiRestants = result.liniaEdis();
            transit = result.liniesTransit();
        }

        // Unió de totes les dates úniques de les dues llistes (sistema + EDI + deute).
        // Fem servir TreeSet per tenir-les ordenades cronològicament.
        Set<LocalDate> dates = new TreeSet<>();
        dates.addAll(req.liniesComandaActual().stream()
                .map(ObtenirLiniesComandaPendentsResponse::dataSolicitada)
                .collect(Collectors.toSet()));

        dates.addAll(liniesEdiRestants.stream()
                .map(l -> l.dataSortidaAMES(req.isDuesDates()))
                .collect(Collectors.toSet())
        );

        // Afegim també les dates de deute, ja que si el sistema no té comandes pendents
        // s'afegeix una línia de deute amb data d'avui que cal mostrar.
        dates.addAll(deuteList.stream()
                .map(LiniesDeute::dataSolicitada)
                .collect(Collectors.toSet()));

        // Filtrem les dates que superen el límit de dies de tall configurat.
        // Evitem mostrar línies massa llunyanes en el temps.
        dates = dates.stream()
                .filter(f -> !f.isAfter(req.dataLimitDiesTall()))
                .collect(Collectors.toCollection(TreeSet::new));

        var comanda = buildComanda(dates, req.liniesComandaActual(), liniesEdiRestants, req.numComanda(), req.infoSortida(), deuteList);
        var comandatransit = MergeComandaResponseImpl.builder()
                .liniesComanda(comanda)
                .liniesTransit(transit)
                .liniesDeute(deuteList)
                .ultimsAlbarans(req.ultimsAlbarans())
                .albaraReferenciaClient(req.albaraReferenciaClient())
                .isConsiderarUltimsAlbarans(req.isConsiderarUltimsAlbarans())
                .isConsiderarAcumulats(req.isConsiderarAcumulats())
                .build();

        return comandatransit;
    }

    private List<ComandaEDI2Response> buildComanda(Set<LocalDate> dates, List<ObtenirLiniesComandaPendentsResponse> liniesComandaActual,
                                                   List<LiniaEdi> aplicarTransitEdi, String numComanda, InformacioSortidaEdi infoSortida,
                                                   List<LiniesDeute> liniesDeute) {
        long acumulatNova = 0L;
        long acumulatActual = 0L;

        List<ComandaEDI2Response> result = new ArrayList<>();

        // Pre-indexem les llistes per data per evitar recórrer-les senceres a cada iteració (O(1) vs O(n)).
        // Només indexem les línies FERM de la comanda actual, ja que les altres es tracten com a "extra".
        Map<LocalDate, ObtenirLiniesComandaPendentsResponse> actualPerData = liniesComandaActual.stream()
                .filter(a -> a.comandaClient().equals(numComanda) && a.tipus().equals(TipusLiniaComanda.FERM))
                .collect(Collectors.toMap(
                        ObtenirLiniesComandaPendentsResponse::dataSolicitada,
                        a -> a,
                        (a, b) -> a // si hi ha duplicats de data, agafem el primer
                ));

        Map<LocalDate, List<LiniaEdi>> ediPerData = aplicarTransitEdi.stream()
                .filter(e -> e.da().fechaInicial().isPresent())
                .collect(Collectors.groupingBy(e -> e.da().fechaInicial().get()));

        Map<LocalDate, LiniesDeute> deutePerData = liniesDeute.stream()
                .collect(Collectors.toMap(
                        LiniesDeute::dataSolicitada,
                        d -> d,
                        (a, b) -> a // si hi ha duplicats de data, agafem el primer
                ));

        for (LocalDate fecha : dates) {

            Optional<ObtenirLiniesComandaPendentsResponse> actual =
                    Optional.ofNullable(actualPerData.get(fecha));

            Optional<LiniaEdi> edi = ediPerData.getOrDefault(fecha, List.of())
                    .stream()
                    .findFirst();

            Optional<LiniesDeute> deute =
                    Optional.ofNullable(deutePerData.get(fecha));

            if (actual.isEmpty() && edi.isEmpty() && deute.isEmpty()) {
                continue;
            }

            if (actual.isPresent()
                    && actual.get().quantitatPendent() == 0
                    && edi.isEmpty()
                    && deute.isEmpty()) {
                continue;
            }

            acumulatActual += actual
                    .map(ObtenirLiniesComandaPendentsResponse::quantitatPendent)
                    .orElse(0L);

            long quantitatNova = actual.isPresent()
                    ? 0L
                    : edi.map(LiniaEdi::quantitatNova)
                    .orElse(deute.map(LiniesDeute::quantitat).orElse(0L));

            acumulatNova += quantitatNova;

            result.add(
                    toComandaEdiResponse(
                            deute,
                            fecha,
                            actual,
                            edi,
                            numComanda,
                            infoSortida,
                            acumulatNova,
                            acumulatActual,
                            false
                    )
            );
        }

        // Línies extra: tenen un número de comanda diferent o no són de tipus FERM.
        // Es mostren al resultat però no afecten els acumulats (es passen a 0).
        liniesComandaActual.stream()
                .filter(a -> !a.comandaClient().equals(numComanda) || !a.tipus().equals(TipusLiniaComanda.FERM))
                .forEach(a -> result.add(
                        toComandaEdiResponse(Optional.empty(), a.dataSolicitada(), Optional.of(a), Optional.empty(),
                                a.comandaClient(), infoSortida, 0L, 0L, true)));

        // Ordenem el resultat final per data sol·licitada
        result.sort(Comparator.comparing(ComandaEDI2Response::dataSortida));

        return result;
    }

    private ComandaEDI2Response toComandaEdiResponse(Optional<LiniesDeute> deute, LocalDate fecha,
                                                     Optional<ObtenirLiniesComandaPendentsResponse> actual,
                                                     Optional<LiniaEdi> edi, String numComanda,
                                                     InformacioSortidaEdi infoSortida, long acumulatNova, long acumulatActual,
                                                     boolean isNeutre) {
        long quantitatNova = isNeutre
                ? actual.map(ObtenirLiniesComandaPendentsResponse::quantitatPendent).orElse(0L)
                : edi.map(LiniaEdi::quantitatNova).orElse(0L);

        Optional<LocalDate> dataPrevistaSortidaInterna;
        boolean tieneTerceraFecha = actual.stream()
                .anyMatch(l -> l.dataPrevistaSortidaInterna().isPresent());

        if (!tieneTerceraFecha) {
            dataPrevistaSortidaInterna = Optional.empty();
        } else if (actual.flatMap(ObtenirLiniesComandaPendentsResponse::dataPrevistaSortidaInterna).isPresent()) {
            dataPrevistaSortidaInterna =
                    actual.flatMap(ObtenirLiniesComandaPendentsResponse::dataPrevistaSortidaInterna);
        } else if (edi.isPresent()) {
            dataPrevistaSortidaInterna =
                    Optional.of(infoSortida.calcularDies(fecha));
        } else {
            dataPrevistaSortidaInterna = Optional.empty();
        }

        return ComandaEDI2ResponseImpl.builder()
                .clauLinia(actual.map(ObtenirLiniesComandaPendentsResponse::clauLinia))
                .comandaClient(numComanda)
                .idLinia(edi.map(LiniaEdi::idLinia))
                .tipusLiniaComanda(actual.map(ObtenirLiniesComandaPendentsResponse::tipus).orElse(TipusLiniaComanda.FERM))
                .quantitatActual(actual.map(ObtenirLiniesComandaPendentsResponse::quantitatPendent).orElse(0L))
                .quantitatNova(quantitatNova) // Calculme la quantitat en funcio de si la linia es neutre o no, en el cas
                // que no sigui cal posar la quantitat que vigui del edi o 0 en cas de que no vingui res. Si es neutre
                // cal igualar les quantitats actual i nova, tenint en compte que la qunatitat que mana es la nova.
                .dataSolicitada(fecha)
                // La data de sortida es calcula amb els dies de trànsit si ve de l'EDI;
                // si no, s'agafa la data prevista del sistema o avui com a fallback.
                .dataSortida(
                    edi.isPresent()
                        ? infoSortida.calcularDies(fecha)
                        : actual
                        .map(ObtenirLiniesComandaPendentsResponse::dataPrevistaSortida)
                        .orElse(infoSortida.calcularDies(LocalDate.now()))
                )
                .dataPrevistaSortidaInterna(dataPrevistaSortidaInterna)
                .dataConfirmada(actual.flatMap(ObtenirLiniesComandaPendentsResponse::dataConfirmadaFabrica))
                .acumulatNova(acumulatNova)
                .acumulatActual(acumulatActual)
                .observacions("")
                .isNeutre(isNeutre) //podem fer servir aquesta condicio ja que previament hem filtrat les linies que no interfereixen a la comanda de les que si.
                .comentarisInterns(
                        actual.map(ObtenirLiniesComandaPendentsResponse::clauLinia).isEmpty()
                            ? edi.get().comentarisInterns()
                            : actual.map(ObtenirLiniesComandaPendentsResponse::comentarisInterns).orElse(null)
                )
                .comentarisClient(
                    actual.map(ObtenirLiniesComandaPendentsResponse::clauLinia).isEmpty()
                        ? edi.get().comentarisClient()
                        : actual.map(ObtenirLiniesComandaPendentsResponse::comentarisClient).orElse(null)
                )
                .infoArticle(actual.map(ObtenirLiniesComandaPendentsResponse::preu))
                .comandaBlanca(actual.flatMap(ObtenirLiniesComandaPendentsResponse::comandaBlanca))
                .empresa(actual.map(ObtenirLiniesComandaPendentsResponse::empresa))
                .referencia(actual.map(ObtenirLiniesComandaPendentsResponse::referencia))
                .clientNom(actual.map(ObtenirLiniesComandaPendentsResponse::clientNom))
                .isPreuFixat(actual.map(ObtenirLiniesComandaPendentsResponse::isPreuFixat))
                .build();
    }
}
package ames.comercial.ofs.internal.domain.service;

import ames.comercial.ofs.internal.domain.Termini;
import ames.comercial.ofs.internal.domain.TerminiImpl;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AplicarNousTerminis {

    List<Termini> terminisAnteriors;
    List<Termini> terminisNous;

    public AplicarNousTerminis(List<Termini> terminisAnteriors, List<Termini> terminisNous) {
        this.terminisAnteriors = terminisAnteriors;
        this.terminisNous = terminisNous;
    }

    public List<Termini> executar() {
        // Agrupacions dels terminis
        var agrupacioAnterior = agrupaLiniesPerTerminis(terminisAnteriors);
        var agrupacioNova = agrupaLiniesPerTerminis(terminisNous);

        var result = new ArrayList<Termini>();
        // Obtenció de les dates que estan a les dos agrupacions
        var totesDates = calculaTotesDates(agrupacioAnterior, agrupacioNova);
        for (var data : totesDates) {
            // Incloure el termini de client
            var termAnteriorClient = findByData(agrupacioAnterior, data, false);
            var termNouClient = findByData(agrupacioNova, data, false);
            buildTerminiAnteriorNou(termAnteriorClient, termNouClient).ifPresent(result::add);
            // Incloure el termini d'stock de seguretat
            var termAnteriorStockSeg = findByData(agrupacioAnterior, data, true);
            var termNouStockSeg = findByData(agrupacioNova, data, true);
            buildTerminiAnteriorNou(termAnteriorStockSeg, termNouStockSeg).ifPresent(result::add);
        }

        return result;
    }

    private Optional<Termini> buildTerminiAnteriorNou(Optional<AgrupacioTermini> optAgrupacioAnterior, Optional<AgrupacioTermini> optAgrupacioNova) {
        // En cas que no hagi cap agrupació no es crea res
        if (optAgrupacioAnterior.isEmpty() && optAgrupacioNova.isEmpty())
            return Optional.empty();

        if (optAgrupacioAnterior.isPresent() && optAgrupacioNova.isPresent()) {
            // En cas que hagi terminis agrupats en l'anterior i l'actual es crea un nou amb la informació de l'anterior
            var agrupacioAnterior = optAgrupacioAnterior.get();
            var agrupacioNova = optAgrupacioNova.get();
            return Optional.of(TerminiImpl.builder()
                    .data(agrupacioNova.data)
                    .dataSortida(agrupacioNova.dataSortida)
                    .quantitat(agrupacioNova.quantitat)
                    .quantitatAnterior(agrupacioAnterior.quantitat)
                    .quantitatRebuda(0L)
                    .isStockSeguretat(agrupacioNova.isStockSeguretat)
                    .build());
        } else if (optAgrupacioAnterior.isPresent()) {
            // En cas que havien terminis agrupats en l'anterior i no en l'actual es crea un nou amb la quantitat a 0
            var agrupacioAnterior = optAgrupacioAnterior.get();
            return Optional.of(TerminiImpl.builder()
                    .data(agrupacioAnterior.data)
                    .dataSortida(agrupacioAnterior.dataSortida)
                    .quantitat(0L)
                    .quantitatAnterior(agrupacioAnterior.quantitat)
                    .quantitatRebuda(0L)
                    .isStockSeguretat(agrupacioAnterior.isStockSeguretat)
                    .build());
        } else {
            // En cas que no havien terminis agrupats en l'anterior i si amb l'actual es crea un nou amb l'anterior a 0
            var agrupacioNova = optAgrupacioNova.get();
            return Optional.of(TerminiImpl.builder()
                    .data(agrupacioNova.data)
                    .dataSortida(agrupacioNova.dataSortida)
                    .quantitat(agrupacioNova.quantitat)
                    .quantitatAnterior(0L)
                    .quantitatRebuda(0L)
                    .isStockSeguretat(agrupacioNova.isStockSeguretat)
                    .build());
        }

    }


    private Optional<AgrupacioTermini> findByData(List<AgrupacioTermini> agrupacio, LocalDate data, boolean isStockSeguretat) {
        return agrupacio.stream()
                .filter(a -> a.data.equals(data)) // Filtre per data
                .filter(a -> a.isStockSeguretat == isStockSeguretat) // Si es busca per stock de seguretat o no
                .findAny();
    }

    private Set<LocalDate> calculaTotesDates(List<AgrupacioTermini> anterior, List<AgrupacioTermini> nova) {
        return Stream.concat(anterior.stream(), nova.stream())
                .map(AgrupacioTermini::data)
                .collect(Collectors.toCollection(TreeSet::new));    // El TreeSet elimina duplicats i ordena per defecte
    }

    private List<AgrupacioTermini> agrupaLiniesPerTerminis(List<Termini> terminis) {
        // Agrupació de les línies de comanda pendents per data i stock de seguretat. Es a dir cada clau representa
        // una data i l'indicador de si és o no stock de seguretat. De manera que per una mateixa dat pot haber
        // dos entrades, una per stock de seguretat i altre que no
        Map<String, List<Termini>> agrupacio = terminis.stream()
                .filter(Termini::isPendent)
                .collect(Collectors.groupingBy(r ->
                        r.data().toString() + "-" + r.isStockSeguretat()));

        List<AgrupacioTermini> resultat = new ArrayList<>();
        for (List<Termini> grup : agrupacio.values()) {
            var data = grup.get(0).data();
            var isStockSeguretat = grup.get(0).isStockSeguretat();

            var qtatTotal = grup.stream().mapToLong(Termini::quantitatPendent).sum();
            var dataSortida = grup.stream().map(Termini::data).min(Comparator.naturalOrder()).orElseThrow();

            resultat.add(new AgrupacioTermini(data,
                    isStockSeguretat,
                    qtatTotal,
                    dataSortida));
        }

        return resultat.stream().sorted(Comparator.comparing(AgrupacioTermini::data)
                        .thenComparing(AgrupacioTermini::isStockSeguretat))
                .toList();
    }

    /**
     * Serveix per fer una agrupació de les línies de comanda per després transformar-les en terminis
     *
     * @param data Data que será de la OF
     * @param isStockSeguretat true si és stock de seguretat, false altrament
     * @param quantitat Quantitat (suma de les quantitats de l'agrupació)
     * @param dataSortida Data sortida (Data més antiga de l'agrupació)
     */
    private record AgrupacioTermini (LocalDate data, boolean isStockSeguretat, long quantitat, LocalDate dataSortida) {}


}

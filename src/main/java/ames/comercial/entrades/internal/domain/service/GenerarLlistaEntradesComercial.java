package ames.comercial.entrades.internal.domain.service;

import ames.comercial.entrades.internal.domain.EntradaComercial;
import ames.comercial.entrades.internal.domain.EntradaMagatzem;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GenerarLlistaEntradesComercial {

    List<EntradaMagatzem> entradaMagatzems;

    public GenerarLlistaEntradesComercial (List<EntradaMagatzem> entradaMagatzems) {
        this.entradaMagatzems = entradaMagatzems;
    }

    public List<EntradaComercial> generar() {
        // Agrupació de les entrades de magatzem
        Map<ClauAgrupacio, List<EntradaMagatzem>> agrupacio = entradaMagatzems.stream()
                .collect(Collectors.groupingBy(e ->
                    new ClauAgrupacio(e.fabrica(),
                            e.articleFabrica(),
                            e.client(),
                            e.of(),
                            e.dataEntrada())
                ));

        return agrupacio.values().stream()
                .map(listEntrades -> {
                    // Entrada de referència per crear l'entrada de comercial
                    var entradaReferencia = listEntrades.get(0);
                    // Sumatori de la quantitat
                    long quantitat = listEntrades.stream().mapToLong(EntradaMagatzem::quantitat).sum();

                    return EntradaComercial.of(entradaReferencia, quantitat);
                }).collect(Collectors.toList());
    }

    private record ClauAgrupacio(String fabrica, String article, String client, long of, LocalDate dataEntrada) {};

}

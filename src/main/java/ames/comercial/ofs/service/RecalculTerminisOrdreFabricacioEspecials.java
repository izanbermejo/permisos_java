package ames.comercial.ofs.service;

import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse;
import ames.comercial.ofs.internal.domain.Termini;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class RecalculTerminisOrdreFabricacioEspecials {

    long stock;
    List<ObtenirLiniesComandaPendentsResponse> liniesComanda;

    public RecalculTerminisOrdreFabricacioEspecials(long stock, List<ObtenirLiniesComandaPendentsResponse> liniesComanda) {
        this.stock = stock;
        this.liniesComanda = liniesComanda;
    }

    public List<Termini> executar() {
        var resultat = new ArrayList<Termini>();
        var stockDisponible = stock;
        // En cas que l'stock sigui negatiu es crea un primer termini amb data d'avui
        // amb la quantitat negativa. Es deixa a 0 l'stock per a que es generin els terminis
        // per cada línia de comanda pendent
        if (stockDisponible < 0) {
            resultat.add(Termini.nou(Math.abs(stockDisponible), LocalDate.now()));
            stockDisponible = 0;
        }

        // Agrupació de les línies per a que només hagi com a màxim 2 línies per data (una de client i altre d'stock de seguretat)
        var liniesPerTermini = agrupaLiniesPerTerminis(liniesComanda);

        for (var linia : liniesPerTermini) {
            // L'stock sobrant és el resultat de restar l'stock disponible
            // i la quantitat pendent de servir de la línia
            var stockSobrant = stockDisponible - linia.quantitat();
            if (stockSobrant < 0) {
                // En cas que no sobri stock cal generar un termini
                resultat.add(Termini.nou(Math.abs(stockSobrant), linia.isStockSeguretat, linia.data, linia.dataSortida));
            }
            // En cas que no quedi stock sobrant es manté a 0
            stockDisponible = Math.max(0, stockSobrant);
        }

        return resultat.stream().sorted(
                    Comparator.comparing(Termini::data) // Ordenació per data
                            .thenComparing(Termini::isStockSeguretat) // En cas de la mateixa data primer las de client
                            .thenComparing(Comparator.comparing(Termini::quantitat).reversed()) // En cas igual les dos condicions anteriors per quantitat ascendent
        ).toList();
    }

    private List<AgrupacioTermini> agrupaLiniesPerTerminis(List<ObtenirLiniesComandaPendentsResponse> liniesComanda) {
        // Agrupació de les línies de comanda per data i stock de seguretat. Es a dir cada clau representa
        // una data i l'indicador de si és o no stock de seguretat. De manera que per una mateixa dat pot haber
        // dos entrades, una per stock de seguretat i altre que no
        Map<String, List<ObtenirLiniesComandaPendentsResponse>> agrupacio = liniesComanda.stream()
                .collect(Collectors.groupingBy(r ->
                        r.dataPerOf().toString() + "-" + r.isStockSeguretat()));

        List<AgrupacioTermini> resultat = new ArrayList<>();
        for (List<ObtenirLiniesComandaPendentsResponse> grup : agrupacio.values()) {
            var data = grup.get(0).dataPerOf();
            var isStockSeguretat = grup.get(0).isStockSeguretat();

            var qtatTotal = grup.stream().mapToLong(ObtenirLiniesComandaPendentsResponse::quantitatPendent).sum();
            var dataSortida = grup.stream().map(ObtenirLiniesComandaPendentsResponse::dataSortidaPerOf).min(Comparator.naturalOrder()).orElseThrow();

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

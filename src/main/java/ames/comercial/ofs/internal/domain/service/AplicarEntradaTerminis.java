package ames.comercial.ofs.internal.domain.service;

import ames.comercial.ofs.internal.domain.Termini;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AplicarEntradaTerminis {

    List<Termini> terminis;

    public AplicarEntradaTerminis(List<Termini> terminis) {
        this.terminis = terminis;
    }

    public List<Termini> executar (long quantitatEntrada) {
        var result = new ArrayList<Termini>();
        // Ordenació dels terminis per data i donant prioritat
        // als terminis de client
        var listTerminisOrdenada = terminis.stream()
                .sorted(Comparator.comparing(Termini::data)
                        .thenComparing(Termini::isStockSeguretat))
                .toList();
        var qtat = quantitatEntrada;
        for (var t : listTerminisOrdenada) {
            // Per cada termini s'aplica l'entrada
            var aplicaEntrada = new AplicarEntradaTermini(t).executar(qtat);
            qtat = aplicaEntrada.quantitatExces();
            result.add(aplicaEntrada.termini());
        }
        return result;
    }

}

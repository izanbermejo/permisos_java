package ames.comercial.comandes.internal.domain.service;

import ames.comercial.comandes.internal.domain.linia.InformacioReserva;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.shared.KeyArticleClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CalculStockAlliberat {

    private final Map<KeyArticleClient, Long> reservesOriginal;
    private final Map<KeyArticleClient, Long> reservesActualitzat;

    public CalculStockAlliberat (Map<KeyArticleClient, Long> reservesOriginal, Map<KeyArticleClient, Long> reservesActualitzat) {
        this.reservesOriginal = reservesOriginal;
        this.reservesActualitzat = reservesActualitzat;
    }

    public CalculStockAlliberat (List<LiniaComanda> liniesOriginals, List<LiniaComanda> liniesActualitzades) {
        this(liniesOriginals.stream()
                        .collect(Collectors.groupingBy(LiniaComanda::articleClient, //Agrupació per articleClient
                                 Collectors.summingLong(l -> l.reserva().map(InformacioReserva::quantitat).orElse(0L)))),   // Suma reserves
            liniesActualitzades.stream()
                .collect(Collectors.groupingBy(LiniaComanda::articleClient, //Agrupació per articleClient
                        Collectors.summingLong(l -> l.reserva().map(InformacioReserva::quantitat).orElse(0L)))));   // Suma reserves
    }

    public Map<KeyArticleClient, Long> calcula() {
        var result = new HashMap<KeyArticleClient, Long>();
        reservesOriginal.forEach((key, value) -> {
            var reservaActualitzada = reservesActualitzat.getOrDefault(key, 0L);
            var reservaAlliberada = value-reservaActualitzada;
            // Només es torna com a resultat els articlesclients que han alliberat reserva
            if (reservaAlliberada > 0)
                result.put(key, reservaAlliberada);
        });
        return result;
    }

}

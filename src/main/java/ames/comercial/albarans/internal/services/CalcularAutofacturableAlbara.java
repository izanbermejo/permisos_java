package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransSortida.CalcularCreacioAlbaransSortidaResponse.CreacioNovaLinia;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Decideix si un albarà de sortida ha de ser autofacturable i, si no ho és, per quin motiu.
 * <p>
 * Un albarà és autofacturable només si el client està marcat com a tal a Advantage (cli6.autofac = 'S')
 * i cap de les seves línies té el preu fixat ni prové d'una comanda blanca (aquests casos s'han de
 * facturar manualment). És la lògica compartida entre la creació ({@code CrearAlbara}) i la
 * previsualització ({@code PrevisualitzarCreacioAlbara}) perquè la decisió que es mostra i la que es
 * persisteix siguin idèntiques.
 */
@Service
public class CalcularAutofacturableAlbara {

    public Resultat calcular(boolean clientFacturacioAutomatica, List<CreacioNovaLinia> liniesAlbara) {
        if (!clientFacturacioAutomatica) {
            return new Resultat(false, Motiu.CLIENT_NO_AUTOFACTURABLE);
        }
        boolean teLiniaNoAutofacturable = liniesAlbara.stream()
                .flatMap(linia -> linia.liniesComanda().stream())
                .anyMatch(lc -> lc.isPreuFixat() || lc.comandaBlanca().isPresent());
        if (teLiniaNoAutofacturable) {
            return new Resultat(false, Motiu.LINIA_PREU_FIXAT_O_COMANDA_BLANCA);
        }
        return new Resultat(true, null);
    }

    public enum Motiu {
        /** El client no està marcat com a autofacturable a Advantage. */
        CLIENT_NO_AUTOFACTURABLE,
        /** Alguna línia de l'albarà té el preu fixat o prové d'una comanda blanca. */
        LINIA_PREU_FIXAT_O_COMANDA_BLANCA
    }

    /**
     * @param autofacturable si l'albarà serà autofacturable
     * @param motiu          motiu pel qual no ho és, o {@code null} si sí que ho és
     */
    public record Resultat(boolean autofacturable, Motiu motiu) {}

}

package ames.comercial.inventari.ext;

import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.util.List;

/**
 * Consulta d'stock per magatzem tenint en compte els magatzems intermitjos.
 * <p>
 * Un magatzem "intermig" és un magatzem pont on la mercaderia està de pas cap a un magatzem
 * "final". Aquesta consulta retorna, per cada (empresa, magatzem), l'stock propi del magatzem MÉS
 * l'stock que hi ha en trànsit als seus magatzems intermitjos, de manera que el consumidor rep
 * directament l'stock total disponible al magatzem final sense haver de fer cap agregació.
 * <p>
 * Es retorna una fila per cada magatzem amb stock (propi o intermig):
 * <ul>
 *   <li>Els magatzems intermitjos surten com a fila pròpia amb {@code isIntermig() == true} (el seu
 *       stock també queda comptat dins de {@code stockIntermig()} del seu magatzem final).</li>
 *   <li>Un magatzem final surt amb {@code stockIntermig() > 0} i, si escau, sense stock propi.</li>
 * </ul>
 * Els consumidors que no vulguin comptar dos cops l'stock d'un intermig (p. ex. el càlcul de
 * traspassos) han de filtrar les files amb {@code isIntermig() == true} i usar {@code stockTotal()}.
 */
public interface IObtenirStockMagatzemsIntermig {

    List<StockMagatzemIntermigResponse> perArticleClient(KeyArticleClient articleClient);

    List<StockMagatzemIntermigResponse> perClients(List<String> clicods);

    @JsonDeserialize(builder = StockMagatzemIntermigResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface StockMagatzemIntermigResponse {
        String artint();
        String clicod();
        String empresa();
        String magatzem();
        /** Stock propi d'aquest magatzem. */
        long stockPropi();
        long stockReservat();
        /** Cert si aquest magatzem és un magatzem intermig d'un altre (final). */
        boolean isIntermig();
        /** Codis dels magatzems intermitjos que alimenten aquest magatzem (buit si no en té). */
        List<String> magatzemsIntermitjos();
        /** Stock en trànsit als magatzems intermitjos d'aquest magatzem (0 si no en té). */
        long stockIntermig();

        /** Stock total disponible al magatzem: propi + intermitjos. */
        @Derived
        default long stockTotal() {
            return stockPropi() + stockIntermig();
        }

        /** Cert si aquest magatzem té algun magatzem intermig que l'alimenta. */
        @Derived
        default boolean teMagatzemIntermig() {
            return stockIntermig() != 0 || !magatzemsIntermitjos().isEmpty();
        }
    }

}

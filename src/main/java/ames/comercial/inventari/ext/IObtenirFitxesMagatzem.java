package ames.comercial.inventari.ext;

import ames.comercial.inventari.ext.IObtenirStockMagatzemsIntermig.StockMagatzemIntermigResponse;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.util.List;

public interface IObtenirFitxesMagatzem {

    ObtenirFitxesMagatzemResponse executar(KeyArticleClient articleClient);

    @JsonDeserialize(builder = ObtenirFitxesMagatzemResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface ObtenirFitxesMagatzemResponse {
        List<FitxaMagatzemResponse> fitxes();
        List<FitxaMagatzemSatelitResponse> fitxesSatelit();

        /**
         * Cert si algun magatzem té stock en trànsit a un magatzem intermig. El frontend només
         * mostra les columnes d'stock intermig i total quan aquesta bandera és certa, per evitar
         * confusió quan no hi ha cap magatzem intermig en joc.
         */
        @Derived
        default boolean hiHaMagatzemIntermig() {
            return fitxes().stream().anyMatch(f -> f.stockIntermig() != 0);
        }
    }

    @JsonDeserialize(builder = FitxaMagatzemResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface FitxaMagatzemResponse {
        String empresa();
        String magatzem();
        /** Stock propi del magatzem. */
        Long stock();
        Long stockReservat();
        /** Stock en trànsit als magatzems intermitjos d'aquest magatzem (0 si no en té). */
        Long stockIntermig();
        /** Codis dels magatzems intermitjos que alimenten aquest magatzem (buit si no en té). */
        List<String> magatzemsIntermitjos();

        /** Stock total disponible al magatzem: propi + intermitjos. */
        @Derived
        default Long stockTotal() {
            return stock() + stockIntermig();
        }

        static FitxaMagatzemResponse of (StockMagatzemIntermigResponse reg) {
            return FitxaMagatzemResponseImpl.builder()
                    .empresa(reg.empresa())
                    .magatzem(reg.magatzem())
                    .stock(reg.stockPropi())
                    .stockReservat(reg.stockReservat())
                    .stockIntermig(reg.stockIntermig())
                    .addAllMagatzemsIntermitjos(reg.magatzemsIntermitjos())
                    .build();
        }

    }

    @JsonDeserialize(builder = FitxaMagatzemSatelitResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface FitxaMagatzemSatelitResponse {
        String magatzem();
        Long stock();
    }

}

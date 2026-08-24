package ames.comercial.comandes.ext;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IObtenirLiniesComandaPendents {

    List<ObtenirLiniesComandaPendentsResponse> executar(KeyArticleClient articleClient);
    List<ObtenirLiniesComandaPendentsResponse> executar(KeyArticleClient articleClient, boolean incloureStockSeguretat);

    @JsonDeserialize(builder = ObtenirLiniesComandaPendentsResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface ObtenirLiniesComandaPendentsResponse {

        KeyLiniaComanda clauLinia();
        Empresa empresa();
        String comandaClient();
        Long quantitatSolicitada();
        Long quantitatPendent();
        TipusLiniaComanda tipus();
        LocalDate dataSolicitada();
        LocalDate dataPrevistaSortida();
        Optional<LocalDate> dataPrevistaSortidaInterna();
        Optional<LocalDate> dataConfirmadaFabrica();
        Optional<String> comentarisInterns();
        Optional<String> comentarisClient();
        Preu preu();
        Optional<Long> comandaBlanca();
        String referencia();
        String clientNom();
        Boolean isPreuFixat();

        @Derived
        default LocalDate dataPrevistaSortidaFinal() {
            return dataPrevistaSortidaInterna().orElse(dataPrevistaSortida());
        }

        @Derived
        default boolean isStockSeguretat() {
            return tipus().isStockSeguretat();
        }

        /**
         * Retorna la data que s'ha de tenir en compte per fer el càlcul de la OF.
         * Data confirmada, si no té data sortida del primer magatzem d'AMES i
         * si no té la data sortida de l'últim magatzem d'AMES
         *
         * @return Data que s'ha de tenir en compte per fe rel càlcul de la OF
         */
        @Derived
        default LocalDate dataPerOf() {
            // En cas que hagi data confirmada de fàbrica aquesta serà la data per a la OF
            if (dataConfirmadaFabrica().isPresent())
                return dataConfirmadaFabrica().get();
            // En cas que no hagi data confirmada serà la data prevista final menys 2 dies
            var data = dataPrevistaSortidaFinal();
            LocalDate novaData = data.minusDays(2);
//            // Es resta un determinat número de dias en funció si es dissabte o diumenge
//            // o es deixa igual
//            if (novaData.getDayOfWeek() == DayOfWeek.SATURDAY) {
//                return novaData.minusDays(1);
//            } else if (novaData.getDayOfWeek() == DayOfWeek.SUNDAY) {
//                return novaData.minusDays(2);
//            }
            return novaData;
        }

        /**
         * Retorna la data que s'ha de tenir en compte per fer el càlcul de la OF
         * per la data de sortida.
         *
         * @return Data de sortida que s'ha de tenir en compte per fe rel càlcul de la OF
         */
        @Derived
        default LocalDate dataSortidaPerOf() {
            // Cal restar dos dies a la data de sortida prevista final
            LocalDate novaData = dataPrevistaSortidaFinal().minusDays(2);
//            // Es resta un determinat número de dias en funció si es dissabte o diumenge
//            // o es deixa igual
//            if (novaData.getDayOfWeek() == DayOfWeek.SATURDAY) {
//                return novaData.minusDays(1);
//            } else if (novaData.getDayOfWeek() == DayOfWeek.SUNDAY) {
//                return novaData.minusDays(2);
//            }
            return novaData;
        }

    }

}

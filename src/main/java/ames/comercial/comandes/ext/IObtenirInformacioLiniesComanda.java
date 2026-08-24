package ames.comercial.comandes.ext;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.shared.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IObtenirInformacioLiniesComanda {

    List<InformacioLiniaComandaDTO> executar(List<KeyLiniaComanda> ids);

    @JsonDeserialize(builder = InformacioLiniaComandaDTOImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface InformacioLiniaComandaDTO {
        KeyLiniaComanda id();
        KeyArticleClient articleClient();
        String empresa();
        String projecte();
        String programa();
        String comandaSegonsClient();
        Adresa adresa();
        InformacioEnviament informacioEnviament();
        TipusArticleClient tipusArticleClient();
        String referencia();
        String nivellTecnic();
        String denominacio();
        String codiPartidaArantzelaria();
        String partidaArantzelaria();
        Optional<String> codiEan13();
        boolean isVolUdi();
        int diesCaducitat();
        Optional<String> codiFamilia();
        TipusLiniaComanda tipus();
        long quantitat();
        long quantitatServida();
        long quantitatReservada();
        /**
         * Unitats del nivell base d'embalatge (Advantage {@code ACLUCAI}); 0 si no n'hi ha.
         * Als no normalitzats són les peces per caixa; als normalitzats, les peces per bossa
         * (les peces per caixa surten de multiplicar-les per {@link #bossesCaixa()}).
         */
        long unitatsEmbalatge();
        /** Bosses per caixa de l'article-client (Advantage {@code BOSXCAI}); 0 si no n'hi ha */
        long bossesCaixa();
        /** Caixes per palet de l'article-client (Advantage {@code ACLUCAP}); 0 si no n'hi ha */
        long caixesPalet();
        /** Pes unitari de la peça en grams (Advantage {@code ART.ARTPFIN}) */
        BigDecimal pesUnitari();
        Preu preu();
        boolean isPreuFixat();
        /**
         * Descompte comercial en % de la línia ({@code dades_calcul.descompte}); 0 quan no n'hi ha.
         * El {@link #preu()} és el brut de tarifa, així que cal aquest percentatge per saber què es
         * facturarà. S'aplica també amb el preu fixat manualment.
         */
        BigDecimal descompte();
        LocalDate dataSolicitada();
        Optional<Long> comandaBlanca();
        @Derived default long quantitatPendent() { return Math.max(0, quantitat() - quantitatServida()); }

        /**
         * Quantitat pendent de servir, en el cas dels normalitzats és la quantitat reservada i sinó la pendent de servir és la pendent total (quantitat - quantitat servida)
         *
         * @return Quantitat pendent de servir
         */
        @Derived default long quantitatServir() {
            return articleClient().isNormalitzat()
                    ? quantitatReservada()
                    : quantitatPendent();
        }
    }

}

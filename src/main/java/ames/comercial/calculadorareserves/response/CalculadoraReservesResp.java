package ames.comercial.calculadorareserves.response;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.util.List;

@JsonDeserialize(builder = CalculadoraReservesRespImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CalculadoraReservesResp {

    List<LiniaCalculReservesResp> linies();

    default LiniaCalculReservesResp linia(KeyLiniaComanda clauLinia) {
        return linies().stream()
                .filter(l -> l.clauLinia() == clauLinia)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("ID línia no calculada"));
    }

    /**
     * @return Quantitat d'stock que es pot reservar en les línies calculades
     */
    @Derived
    default long stockReservat() {
        return linies().stream()
                .map(LiniaCalculReservesResp::quantitatReservada)
                .reduce(Long::sum)
                .orElse(0L);
    }

    /**
     * Representació de les línies calculades
     */
    @JsonDeserialize(builder = LiniaCalculReservesRespImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface LiniaCalculReservesResp {
        KeyLiniaComanda clauLinia();
        long quantitatReservada();

        static LiniaCalculReservesResp of(KeyLiniaComanda clauLinia, long quantitatReservada) {
            return LiniaCalculReservesRespImpl.builder()
                    .clauLinia(clauLinia)
                    .quantitatReservada(quantitatReservada)
                    .build();
        }
    }

}

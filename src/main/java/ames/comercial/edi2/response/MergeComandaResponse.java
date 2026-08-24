package ames.comercial.edi2.response;

import ames.comercial.albarans.internal.application.query.ObtenirUltimsAlbarans.ObtenirUltimsAlbaransResponse;
import ames.comercial.edi2.internal.domain.LiniaTransit;
import ames.comercial.edi2.internal.domain.LiniesDeute;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = MergeComandaResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface MergeComandaResponse {

    List<ComandaEDI2Response> liniesComanda();
    List<LiniaTransit> liniesTransit();
    List<LiniesDeute> liniesDeute();
    Optional<ObtenirUltimsAlbaransResponse> ultimsAlbarans();
    String albaraReferenciaClient();
    boolean isConsiderarUltimsAlbarans();
    boolean isConsiderarAcumulats();

    @Value.Derived
    default long acumulatActual(){
        return liniesComanda().stream()
                .filter(l -> !l.isNeutre())
                .mapToLong(ComandaEDI2Response::quantitatActual)
                .sum();
    }

    @Value.Derived
    default long acumulatNou(){
        return liniesComanda().stream()
                .filter(l -> !l.isNeutre())
                .mapToLong(ComandaEDI2Response::quantitatNova)
                .sum();
    }

    @Value.Derived
    default long diferencia() {
        return acumulatNou() - acumulatActual();
    }

    @Value.Derived
    default BigDecimal percent() {
        BigDecimal actual = BigDecimal.valueOf(acumulatActual());

        if (actual.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(diferencia())
                .multiply(BigDecimal.valueOf(100))
                .divide(actual, 2, RoundingMode.HALF_UP);
    }
}
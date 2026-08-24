package ames.comercial.comandes.internal.infraestructure.variacio;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;

@JsonDeserialize(builder = VariacioComplReqImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface VariacioComplReq {
    String clientCodi();
    String clientDesc();
    String codiPesa();
    String referencia();
    String fabrica();
    String projectManager();
    BigDecimal factEur();
    BigDecimal pes();
    boolean isValid();
}

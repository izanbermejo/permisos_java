package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.util.Optional;

@JsonDeserialize(builder = LBImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LB {

    String inicioRegistro();
    Optional<Long> stockActual();
    Optional<Long> stockSeguridad();
    Optional<String> identificacionArticuloProveedor();
    Optional<String> paisConsignatario();
    Optional<BigDecimal> precio();
    Optional<String> divisa();

}

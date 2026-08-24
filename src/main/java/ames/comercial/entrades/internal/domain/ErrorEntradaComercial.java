package ames.comercial.entrades.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@JsonDeserialize(builder = ErrorEntradaComercialImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ErrorEntradaComercial {

    String idEntradaFabrica();
    LocalDateTime dataError();
    Optional<LocalDateTime> dataAvis();

}

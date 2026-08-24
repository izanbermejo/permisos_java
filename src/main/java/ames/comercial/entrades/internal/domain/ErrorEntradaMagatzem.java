package ames.comercial.entrades.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDateTime;
import java.util.Optional;

@JsonDeserialize(builder = ErrorEntradaMagatzemImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ErrorEntradaMagatzem {

    String idEntradaFabrica();
    String magatzem();
    LocalDateTime dataError();
    Optional<LocalDateTime> dataAvis();

}

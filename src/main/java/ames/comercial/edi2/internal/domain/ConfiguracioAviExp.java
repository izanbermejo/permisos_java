package ames.comercial.edi2.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = ConfiguracioAviExpImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ConfiguracioAviExp {

    String codiClient();
    String codiProveidor();
    boolean volAviExp();
    boolean volEnviarLG();
    Optional<String> ediBox();
}

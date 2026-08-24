package ames.comercial.edi2.internal.domain.capsalera;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = CCImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CC {

    String inicioRegistro();
    String nombreComprador();
    Optional<String> direccionComprador();
    Optional<String> localidadComprador();
    Optional<String> provinciaComprador();
    Optional<String> codigoPostalComprador();

}

package ames.comercial.edi2.internal.domain.capsalera;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = CPImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CP {

    String inicioRegistro();
    Optional<String> nombreProveedor();
    Optional<String> direccionProveedor();
    Optional<String> localidadProveedor();
    Optional<String> provinciaProveedor();
    Optional<String> codPostalProveedor();

}

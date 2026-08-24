package ames.comercial.edi2.internal.domain.capsalera;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = CQImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CQ {

    String inicioRegistro();
    Optional<String> paisProveedor();
    Optional<String> personaContactoProveedor();
    Optional<String> telefonoProveedor();
    Optional<String> faxProveedor();

}

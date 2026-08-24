package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = LSImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LS {

    String inicioRegistro();
    Optional<String> codigoSolicitante();
    Optional<String> nombreSolicitante();
    Optional<String> personaContacto();
    Optional<String> telefono();

}

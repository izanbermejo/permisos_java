package ames.comercial.edi2.internal.domain.capsalera;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = CDImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CD {

    String inicioRegistro();
    Optional<String> paisComprador();
    Optional<String> personaContactoComprador();
    Optional<String> telefonoComprador();
    Optional<String> faxComprador();
    Optional<String> emailComprador();

}
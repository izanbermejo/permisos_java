package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = LCImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LC {

    String inicioRegistro();
    String codigoConsignatario();
    String nombreConsignatario();
    Optional<String> personaContacto();
    Optional<String> telefono();

}

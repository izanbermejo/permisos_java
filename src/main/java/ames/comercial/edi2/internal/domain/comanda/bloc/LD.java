package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = LDImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LD {

    String inicioRegistro();
    Optional<String> direccion();
    Optional<String> localidad();
    Optional<String> provincia();
    Optional<String> codigoPostal();
    Optional<String> fax();

}

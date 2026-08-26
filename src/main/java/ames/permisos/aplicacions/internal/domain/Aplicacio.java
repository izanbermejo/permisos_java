package ames.permisos.aplicacions.internal.domain;

import com.fasterxml.jackson.databind.annotation.*;
import org.immutables.value.*;

@JsonDeserialize(builder = AplicacioImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Aplicacio {
    String nomAplicacio();
    String descripcio();
}

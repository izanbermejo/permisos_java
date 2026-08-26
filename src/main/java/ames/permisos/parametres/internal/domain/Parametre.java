package ames.permisos.parametres.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = ParametreImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Parametre {
    String nomAplicacio();
    String nomParametre();
    String descripcio();
}

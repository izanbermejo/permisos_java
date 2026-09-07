package ames.permisos.parametres.internal.domain;

import ames.permisos.organigrama.internal.domain.Empleat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = EmpleatParametreImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface EmpleatParametre extends Empleat {
    String valor();
}

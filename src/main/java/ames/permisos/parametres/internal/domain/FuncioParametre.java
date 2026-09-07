package ames.permisos.parametres.internal.domain;

import ames.permisos.organigrama.internal.domain.Funcio;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = FuncioParametreImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface FuncioParametre extends Funcio {
    String valor();
}

package ames.permisos.moduls.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = ModulImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Modul {
    String nomAplicacio();
    String nomModul();
    String descripcio();
}

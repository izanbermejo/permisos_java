package ames.permisos.permisos.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = PermisImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Permis {
    String nomAplicacio();
    String nomModul();
    String nomPermis();
    String descripcio();
}

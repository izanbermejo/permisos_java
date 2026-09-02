package ames.permisos.organigrama.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = EmpleatImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface Empleat {
    int id();
    String nom();
    String cognoms();
    String email();
    Optional<String> tipusAssignacio();
}

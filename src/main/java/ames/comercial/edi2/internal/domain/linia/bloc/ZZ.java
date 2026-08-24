package ames.comercial.edi2.internal.domain.linia.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = ZZImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ZZ {

    String inicioRegistro();
    Optional<Long> numeroRegistros();

    int i_inicio = 2;
    int i_numeroRegitros = 12;
}

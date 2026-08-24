package ames.comercial.edi2.internal.domain.capsalera;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = CTImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CT {

    String inicioRegistro();
    String textoLibre();

}

package ames.comercial.edi2.internal.domain.capsalera;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = CFImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CF {

    String inicioRegistro();
    Optional<String> nombreFacturacion();
    Optional<String> departamentoFacturacion();


}

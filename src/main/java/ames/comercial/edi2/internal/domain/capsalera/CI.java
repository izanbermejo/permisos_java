package ames.comercial.edi2.internal.domain.capsalera;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = CIImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CI {

    String inicioRegistro();
    String idComprador();
    String idProveedor();
    Optional<String> numCuentaInternaProveedor();
    String idExpedidor();
    String idFacturado();

}

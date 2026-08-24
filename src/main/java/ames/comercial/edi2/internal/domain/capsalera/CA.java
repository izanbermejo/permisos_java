package ames.comercial.edi2.internal.domain.capsalera;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = CAImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CA {

    String inicioRegistro();
    String tipoMensaje();
    String numeroEnvio();
    String buzonOrigen();
    String buzonDestino();
    String numeroDocumento();
    String codigoDocumento();
    String documento();
    Optional<String> logisticaNombreMensaje();
    Optional<String> funcionMensaje();
    Optional<String> referenciaAplicacion();

}

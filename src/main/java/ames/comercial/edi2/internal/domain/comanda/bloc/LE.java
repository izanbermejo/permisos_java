package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = LEImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LE {

    String inicioRegistro();
    String tipoBulto();
    String referenciaEmbalajeCliente();
    Optional<Long> cantPiezasPorEmbalaje();
    Optional<String> tipoContenedor();
    Optional<Long> numeroEmbalajes();
    Optional<String> nivelEmpaquetamiento();

}

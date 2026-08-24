package ames.comercial.edi2.internal.domain.capsalera;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@JsonDeserialize(builder = CBImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CB {

    String inicioRegistro();
    LocalDate fechaMensaje();
    Optional<LocalTime> horaMensaje();
    Optional<LocalDate> fechaInicioHorizonte();
    Optional<LocalDate> fechaFinalHorizonte();
    Optional<String> tipoFecha();
    Optional<String> idTransportista();
    Optional<String> tipoTransporte();

}
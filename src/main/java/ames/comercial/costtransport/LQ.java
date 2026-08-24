package ames.comercial.costtransport;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LQImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LQ {

    String inicioRegistro(); // "LQ"

    Optional<Long> cantidadBalance();
    Optional<LocalDate> fechaCantidadBalance();

    Optional<Long> cantidadAtraso();
    Optional<LocalDate> fechaCantidadAtraso();

    Optional<Long> cantidadUrgente();
    Optional<LocalDate> fechaCantidadUrgente();

    Optional<Long> cantidadTransito();
    Optional<LocalDate> fechaCantidadTransito();

    Optional<Long> cantAcumRecibida();
    Optional<Long> cantAcumProgramada();

    Optional<LocalDate> inicioPeriodoAcum();
    Optional<LocalDate> finPeriodoAcum();

    Optional<Long> cantAcumPeriodoAnt();

}
package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LQImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LQ {

    String inicio();
    Optional<Long> cantidadBalance();
    Optional<LocalDate> fechaCantidadBalance();
    Optional<Long> cantidadAtraso();
    Optional<LocalDate> fechaCantidadAtraso();
    Optional<Long> cantidadUrgente();
    Optional<LocalDate> fechaCantidadUrgente();
    Optional<Long> cantidadTransito();
    Optional<LocalDate> fechaCantidadTransito();
    Optional<Long> cantidadAcumuladaRecibida();
    Optional<Long> cantidadAcumuladaProgramada();
    Optional<LocalDate> inicioPeriodoAcumulada();
    Optional<LocalDate> finPeriodoAcumulada();
    Optional<Long> cantidadAcumuladaPeriodoAnterior();
}

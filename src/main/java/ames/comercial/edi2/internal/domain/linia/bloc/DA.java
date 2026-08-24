package ames.comercial.edi2.internal.domain.linia.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@JsonDeserialize(builder = DAImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DA {

    String inicioRegistro();
    String tipoDetalle();
    Long cantidad();
    String unidadMedida();
    Optional<LocalDate> fechaInicial();
    Optional<LocalTime> horaInicial();
    Optional<LocalDate> fechaFinal();
    Optional<LocalTime> horaFinal();
    Optional<String> razonInstruccion();
    Optional<String> numeroRan();
    Optional<LocalDate> fechaRan();
    Optional<String> frecuenciaEnvio();
    Optional<String> numTarjetaKanban();
    Optional<String> ultimoNumeroRanEmitido();

}

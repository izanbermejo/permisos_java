package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@JsonDeserialize(builder = LAImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LA {

    String inicioRegistro();
    String idArticuloComprador();
    String estadoArticulo();
    Optional<String> codigoAccion();
    Optional<String> unidadMedida();
    Optional<String> lugarEntrega();
    Optional<String> lugarDestinoFinal();
    Optional<String> codigoAlmacen();
    Optional<LocalDate> fechaCubierta();
    Optional<String> paisOrigenCodificado();
    Optional<LocalDate> fechaLimiteEntrega();
    Optional<LocalDate> fechaCalculoActual();
    Optional<LocalDate> fechaInicioCalculo();
    Optional<String> codigoFrecuenciaEntrega();
    Optional<String> indicadorRequerimiento();
    Optional<String> codigoCaracteristicaItem();
    Optional<LocalTime> horaLimiteEntrega();

}
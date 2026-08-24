package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@JsonDeserialize(builder = AAImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface AA {

    String inicio();
    Optional<String> referenciaAlbaranEntrada();
    Optional<LocalDate> fechaAlbaran();
    Optional<LocalTime> horaAlbaran();
    Optional<String> cantidadEnviadaAlbaran();
    Optional<String> cantidadRecibidaAlbaran();
    Optional<LocalDate> fechaRecepcion();

}

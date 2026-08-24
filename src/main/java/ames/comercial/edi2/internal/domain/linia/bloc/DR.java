package ames.comercial.edi2.internal.domain.linia.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@JsonDeserialize(builder = DRImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DR {

    Optional<LocalDate> fechaEntradaLinea();
    Optional<LocalTime> horaEntradaLinea();

}

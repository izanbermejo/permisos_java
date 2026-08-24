package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LIImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LI {

    String inicioRegistro();
    Optional<String> revisionDiseno();
    Optional<LocalDate> fechaCambioIngenieria();
    Optional<String> numeroRuta();
    Optional<String> sufijoRuta();
    Optional<String> numeroTransporte();

}

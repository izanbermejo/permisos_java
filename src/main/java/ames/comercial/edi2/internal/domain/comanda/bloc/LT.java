package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = LTImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LT {

    String inicioRegistro();
    Optional<String> texto1();
    Optional<String> texto2();
    Optional<String> texto3();
    Optional<String> texto4();

}

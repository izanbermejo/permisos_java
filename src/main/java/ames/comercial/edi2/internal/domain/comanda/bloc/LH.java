package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LHImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LH {

    String inicioRegistro();
    Optional<String> numPedidoPrevio();
    Optional<String> numeroLab();
    Optional<String> numeroLote();
    Optional<LocalDate> fechaLab();
    Optional<LocalDate> fechaPedidoPrevio();
    Optional<String> numeroPedidoNuevo();
    Optional<LocalDate> fechaPedidoNuevo();

}

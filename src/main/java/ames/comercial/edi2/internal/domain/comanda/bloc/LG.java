package ames.comercial.edi2.internal.domain.comanda.bloc;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LGImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LG {

    String inicioRegistro();
    Optional<String> descripcionArticulo();
    String numeroContratoPedido();
    Optional<LocalDate> fechaContratoPedido();
    Optional<String> numeroDocumentoAnterior();
    Optional<LocalDate> fechaDocumentoAnterior();
    Optional<String> numeroLineaContrato();
    Optional<String> numeroPlano();

}

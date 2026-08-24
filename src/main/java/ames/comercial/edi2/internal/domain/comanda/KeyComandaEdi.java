package ames.comercial.edi2.internal.domain.comanda;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = KeyComandaEdiImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface KeyComandaEdi {

    long idMissatge();
    long idComanda();

    static KeyComandaEdi of(long idMissatge, long idComanda) {
        return KeyComandaEdiImpl.builder()
                .idMissatge(idMissatge)
                .idComanda(idComanda)
                .build();
    }
}

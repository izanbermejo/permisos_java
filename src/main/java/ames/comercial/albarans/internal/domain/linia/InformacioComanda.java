package ames.comercial.albarans.internal.domain.linia;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

@JsonDeserialize(builder = InformacioComandaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioComanda {

    Optional<Long> comanda();
    String comandaClient();
    String programa();

    static InformacioComanda of(long comanda, String comandaClient, String programa) {
        return InformacioComandaImpl.builder()
                .comanda(Optional.of(comanda))
                .comandaClient(comandaClient)
                .programa(programa)
                .build();
    }

}


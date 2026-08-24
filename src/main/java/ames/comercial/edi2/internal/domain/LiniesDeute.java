package ames.comercial.edi2.internal.domain;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = LiniesDeuteImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniesDeute {

    Optional<KeyLiniaComanda> liniaComanda();
    LocalDate dataSolicitada();
    long quantitat();
    long acumulat();
    long total();

    @Value.Derived
    default String ratio() {
        return acumulat() + "/" + total();
    }
}

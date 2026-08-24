package ames.comercial.edi2.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;

@JsonDeserialize(builder = LiniaTransitImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface LiniaTransit {

    LocalDate dataSolicitada();
    long quantitat();
    long acumulat();
    long total();

    @Value.Derived
    default String ratio() {
        return acumulat() + "/" + total();
    }
}

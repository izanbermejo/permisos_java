package ames.comercial.albarans.internal.domain.albara;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;

@JsonDeserialize(builder = InformacioTraspasImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioTraspas {

    String magatzemReceptor();
    String empresaReceptora();
    boolean isTraspasAbonable();

}

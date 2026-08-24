package ames.comercial.entrades.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = GuardarAdresesEnviamentRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface GuardarAdresesEnviamentRequest {

    String emailsComercial();
    String emailsMagatzem();

}

package ames.comercial.entrades.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = CanviArticleClientEntradaComercialRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CanviArticleClientEntradaComercialRequest {

    String client();
    String article();

}

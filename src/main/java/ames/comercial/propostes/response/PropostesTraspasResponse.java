package ames.comercial.propostes.response;

import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse.RegArticlesTraspasPendents;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@JsonDeserialize(builder = PropostesTraspasResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface PropostesTraspasResponse {

    String magatzemOrigen();
    String magatzemDesti();
    boolean isMagatzemDestiPlataforma();
    List<RegArticlesTraspasPendents> traspassos();

}

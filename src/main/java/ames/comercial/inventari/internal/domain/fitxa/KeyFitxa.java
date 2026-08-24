package ames.comercial.inventari.internal.domain.fitxa;

import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = KeyFitxaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface KeyFitxa {

    KeyArticleClient articleClient();
    String empresa();
    String magatzem();

    static KeyFitxa of(KeyArticleClient articleClient, String empresa, String magatzem) {
        return KeyFitxaImpl.builder()
                .articleClient(articleClient)
                .empresa(empresa)
                .magatzem(magatzem)
                .build();
    }

}

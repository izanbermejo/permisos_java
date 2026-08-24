package ames.comercial.albarans.internal.domain.albara;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

@JsonDeserialize(builder = KeyAlbaraImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface KeyAlbara {

    long codi();
    String empresa();

    static KeyAlbara of(long codi, String empresa) {
        return KeyAlbaraImpl.builder()
                .codi(codi)
                .empresa(empresa)
                .build();
    }

    @Derived
    default String codiFormat() { return String.format("%07d", codi()); }

}

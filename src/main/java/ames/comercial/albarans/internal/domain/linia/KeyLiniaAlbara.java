package ames.comercial.albarans.internal.domain.linia;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

@JsonDeserialize(builder = KeyLiniaAlbaraImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface KeyLiniaAlbara {

    KeyAlbara idAlbara();
    long linia();

    static KeyLiniaAlbara of (String empresa, long numeroAlbara, long linia) {
        return of(KeyAlbara.of(numeroAlbara, empresa), linia);
    }

    static KeyLiniaAlbara of(KeyAlbara idAlbara, long linia) {
        return KeyLiniaAlbaraImpl.builder()
                .idAlbara(idAlbara)
                .linia(linia)
                .build();
    }

    @Derived
    default String clauLiniaFormat() { return String.format("%s / %s", idAlbara().codiFormat(), liniaFormat()); }

    @Derived
    default String liniaFormat() { return String.format("%03d", linia()); }

}

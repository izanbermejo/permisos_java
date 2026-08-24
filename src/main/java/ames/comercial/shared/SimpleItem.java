package ames.comercial.shared;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

@JsonDeserialize(builder = SimpleItemImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface SimpleItem {

    String codi();
    String nom();

    static SimpleItem of (String codi, String nom) {
        return SimpleItemImpl.builder()
                .codi(codi)
                .nom(nom)
                .build();
    }

    @Derived
    default String codiNom() {
        return String.format("%s - %s", codi(), nom());
    }

}

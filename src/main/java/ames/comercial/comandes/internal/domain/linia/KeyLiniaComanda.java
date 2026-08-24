package ames.comercial.comandes.internal.domain.linia;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

@JsonDeserialize(builder = KeyLiniaComandaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface KeyLiniaComanda {

    long comanda();
    long numero();

    static KeyLiniaComanda of(long comanda, long numero) {
        return KeyLiniaComandaImpl.builder()
                .comanda(comanda)
                .numero(numero)
                .build();
    }

    @Derived
    default String comandaFormat() { return String.format("%07d", comanda()); }

    @Derived
    default String numeroFormat() { return String.format("%04d", numero()); }

}

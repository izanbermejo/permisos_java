package ames.comercial.inventari.internal.domain.moviment;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = InformacioSortidaImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioSortida {

    String client();
    // Tota sortida prové d'una línia de comanda (també els consums, que serveixen la comanda pendent FIFO)
    KeyLiniaComanda liniaComanda();

}

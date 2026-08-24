package ames.comercial.albarans.internal.domain.facturacio;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = FacturacioLiniaAlbaraImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface FacturacioLiniaAlbara {

    KeyLiniaAlbara liniaAlbara();
    String codiFactura();
    long quantitat();

}

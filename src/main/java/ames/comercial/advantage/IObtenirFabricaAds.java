package ames.comercial.advantage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

public interface IObtenirFabricaAds {

    Optional<FabricaAds> get(String codi);
    List<FabricaAds> all();

    @JsonDeserialize(builder = FabricaAdsImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface FabricaAds {
        String codi();
        String descripcio();
        String magatzemEntrada();
    }
}

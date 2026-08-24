package ames.comercial.advantage;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

public interface IObtenirEmpresesAds {
    Optional<EmpresaAds> get(String codi);
    List<EmpresaAds> all();

    @JsonDeserialize(builder = EmpresaAdsImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface EmpresaAds{
        String codi();
        String descripcio();
        String adresaAlbara();
        String poblacioAlbara();
        String paisAlbara();
        String gateComp();
        String dunsEnviament();
        String telalb();
        String nif();

    }

}

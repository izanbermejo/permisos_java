package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirZonesTransportAds.ZonaTransportAds;

import java.util.List;
import java.util.Optional;

public interface IObtenirZonesTransportAds {
    Optional<ZonaTransportAds> get(String codiZona, String codiTransportista, String codiPais);
    List<ZonaTransportAds> get(String codiTransportista, String codiPais);
    List<ZonaTransportAds> all();
}

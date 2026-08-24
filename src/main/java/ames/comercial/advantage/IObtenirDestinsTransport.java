package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirDestinsAds.DestiAds;

import java.util.List;
import java.util.Optional;

public interface IObtenirDestinsTransport {
    Optional<DestiAds> get(String codi);
    List<DestiAds> all();
}

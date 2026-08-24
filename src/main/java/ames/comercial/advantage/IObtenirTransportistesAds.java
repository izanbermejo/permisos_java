package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirTransportistesAds.TransportistaAds;

import java.util.List;
import java.util.Optional;

public interface IObtenirTransportistesAds {
    Optional<TransportistaAds> get(String codi);
    List<TransportistaAds> all();
}

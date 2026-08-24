package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirPaisosAds.PaisAds;

import java.util.List;
import java.util.Optional;

public interface IObtenirPaisosAds {
    Optional<PaisAds> get(String codi);
    List<PaisAds> all();
}

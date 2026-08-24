package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirTarifesDefinidesAds.TarifesDefinidesAdsResponse;

import java.util.Optional;

public interface IObtenirTarifesDefinidesAds {
    Optional<TarifesDefinidesAdsResponse> get(String clicod);
}

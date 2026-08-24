package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;

import java.util.Optional;

public interface IObtenirClientAds {
    Optional<ClientAds> get(String clicod);
}

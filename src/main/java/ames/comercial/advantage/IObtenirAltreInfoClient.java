package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirAltreInfoClient.AltreInfoClient;

import java.util.Optional;

public interface IObtenirAltreInfoClient {
    Optional<AltreInfoClient> get(String clicod);
}

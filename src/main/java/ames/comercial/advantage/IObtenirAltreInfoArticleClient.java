package ames.comercial.advantage;

import ames.comercial.advantage.internal.ObtenirAltreInfoArticleClient.AltreInfoArticleClient;

import java.util.Optional;

public interface IObtenirAltreInfoArticleClient {
    Optional<AltreInfoArticleClient> get(String artCli);
}

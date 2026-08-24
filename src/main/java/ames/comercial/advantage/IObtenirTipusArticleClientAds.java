package ames.comercial.advantage;

import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.TipusArticleClient;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IObtenirTipusArticleClientAds {

    Optional<TipusArticleClient> get(KeyArticleClient articleClient);
    Map<KeyArticleClient, TipusArticleClient> get(Set<KeyArticleClient> articlesClient);

}

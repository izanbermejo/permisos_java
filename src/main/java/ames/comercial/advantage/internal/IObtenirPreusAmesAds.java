package ames.comercial.advantage.internal;

import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;

import java.util.Collection;
import java.util.Map;

public interface IObtenirPreusAmesAds {

    Map<KeyArticleClient, Preu> query(Collection<KeyArticleClient> articles);

}

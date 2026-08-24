package ames.comercial.albarans.internal.services;

import ames.comercial.advantage.internal.IObtenirPreusAds;
import ames.comercial.advantage.internal.IObtenirPreusAmesAds;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class PreusProviderSelector {

    private final IObtenirPreusAmesAds obtenirPreusAmesAds;
    private final IObtenirPreusAds obtenirPreusAds;

    public PreusProviderSelector(
            @Qualifier("obtenirPreusAmesAds") IObtenirPreusAmesAds obtenirPreusAmesAds,
            @Qualifier("obtenirPreusAds") IObtenirPreusAds obtenirPreusAds) {
        this.obtenirPreusAmesAds = obtenirPreusAmesAds;
        this.obtenirPreusAds = obtenirPreusAds;
    }

    public Map<KeyArticleClient, Preu> provide(boolean isFacturable, Collection<KeyArticleClient> articleClients) {
        return isFacturable
                ? obtenirPreusAmesAds.query(articleClients)
                : obtenirPreusAds.query(articleClients);
    }

}


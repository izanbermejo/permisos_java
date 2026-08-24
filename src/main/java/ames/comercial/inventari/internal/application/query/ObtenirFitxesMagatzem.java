package ames.comercial.inventari.internal.application.query;

import ames.comercial.advantage.internal.ObtenirStockEstantsSatelitAds;
import ames.comercial.inventari.ext.IObtenirFitxesMagatzem;
import ames.comercial.inventari.ext.IObtenirFitxesMagatzem.FitxaMagatzemResponse;
import ames.comercial.inventari.ext.IObtenirStockMagatzemsIntermig;
import ames.comercial.inventari.ext.ObtenirFitxesMagatzemResponseImpl;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObtenirFitxesMagatzem implements IObtenirFitxesMagatzem {

    @Autowired IObtenirStockMagatzemsIntermig obtenirStockMagatzemsIntermig;

    @Override
    public ObtenirFitxesMagatzemResponse executar(KeyArticleClient articleClient) {
        return ObtenirFitxesMagatzemResponseImpl.builder()
                .fitxes(obtenirFitxes(articleClient))
                .fitxesSatelit(new ObtenirStockEstantsSatelitAds().query(articleClient))
                .build();
    }

    private List<FitxaMagatzemResponse> obtenirFitxes(KeyArticleClient articleClient) {
        return obtenirStockMagatzemsIntermig.perArticleClient(articleClient)
                .stream()
                .map(FitxaMagatzemResponse::of)
                .toList();
    }

}

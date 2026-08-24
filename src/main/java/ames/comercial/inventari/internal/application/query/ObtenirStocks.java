package ames.comercial.inventari.internal.application.query;

import ames.comercial.inventari.ext.IObtenirStocks;
import ames.comercial.inventari.internal.domain.fitxa.Fitxa;
import ames.comercial.inventari.internal.infraestructure.fitxa.FitxaRepository;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class ObtenirStocks implements IObtenirStocks {

    @Autowired FitxaRepository fitxaRepository;

    @Override
    public long stockTotal(KeyArticleClient articleClient) {
        return fitxaRepository.find(articleClient)
                .stream()
                .mapToLong(Fitxa::stock)
                .sum();
    }

    @Override
    public Map<Empresa, Stock> query(KeyArticleClient articleClient, String magatzem) {
        Map<Empresa, Stock> resultat = new HashMap<>();
        // Obtenció de les fitxes d'inventari per a l'article i filtratge per magatzem
        fitxaRepository.find(articleClient)
                .stream()
                .filter(fitxa -> fitxa.magatzem().equals(magatzem))
                .forEach(fitxa -> resultat.put(fitxa.empresa(), new Stock(fitxa.stock(), fitxa.stockReservat())));
        return resultat;
    }

    @Override
    public Optional<Stock> query(KeyArticleClient articleClient, Empresa empresa) {
        return Optional.ofNullable(query(Set.of(articleClient), empresa).get(articleClient));
    }

    @Override
    public Optional<Stock> query(KeyArticleClient articleClient, Empresa empresa, String magatzem) {
        return Optional.ofNullable(queryMagatzem(Set.of(articleClient), empresa, magatzem).get(articleClient));
    }

    @Override
    public Map<KeyArticleClient, Stock> query(Set<KeyArticleClient> articlesClient, Empresa empresa) {
        Map<KeyArticleClient, Stock> resultat = new HashMap<>();
        // Obtenció de les fitxes d'inventari per a l'empresa (tenint en compte la seva empresa de referència per stock)
        var empresaStock = Empresa.referenciaStock(empresa);
        fitxaRepository.find(articlesClient, empresaStock.clau(), empresaStock.magatzem())
                .forEach(fitxa -> resultat.put(fitxa.articleClient(), new Stock(fitxa.stock(), fitxa.stockReservat())));
        return resultat;
    }

    @Override
    public Map<KeyArticleClient, Stock> queryMagatzem(Set<KeyArticleClient> articlesClient, Empresa empresa, String magatzem) {
        Map<KeyArticleClient, Stock> resultat = new HashMap<>();
        // Obtenció de les fitxes d'inventari per a l'empresa (tenint en compte la seva empresa de referència per stock)
        fitxaRepository.find(articlesClient, empresa.clau(), magatzem)
                .forEach(fitxa -> resultat.put(fitxa.articleClient(), new Stock(fitxa.stock(), fitxa.stockReservat())));
        return resultat;
    }

}

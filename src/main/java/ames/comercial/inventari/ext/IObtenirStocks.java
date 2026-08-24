package ames.comercial.inventari.ext;

import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Stock;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IObtenirStocks {

    long stockTotal(KeyArticleClient articleClient);

    Map<Empresa, Stock> query(KeyArticleClient articleClient, String magatzem);

    Optional<Stock> query(KeyArticleClient articleClient, Empresa empresa);

    Optional<Stock> query(KeyArticleClient articleClient, Empresa empresa,String magatzem);

    Map<KeyArticleClient, Stock> query(Set<KeyArticleClient> articleClient, Empresa empresa);

    Map<KeyArticleClient, Stock> queryMagatzem(Set<KeyArticleClient> articlesClient, Empresa empresa, String magatzem);
}

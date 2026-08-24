package ames.comercial.comandes.service;

import ames.comercial.inventari.ext.IObtenirStocks;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ProviderStocks implements IProviderStocks {

	@Autowired IObtenirStocks obtenirStocks;

	@Override
	public long provide(KeyArticleClient articleClient, String magatzem) {
		return obtenirStocks.query(articleClient, magatzem)
				.values()
				.stream()
				.mapToLong(Stock::stock)
				.sum();
	}

	@Override
	public Map<KeyArticleClient, Stock> calculate(Set<KeyArticleClient> articles, Empresa empresa) {
		return obtenirStocks.query(articles, empresa);
	}

}

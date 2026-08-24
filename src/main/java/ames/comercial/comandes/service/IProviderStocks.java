package ames.comercial.comandes.service;

import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Stock;

import java.util.Map;
import java.util.Set;

public interface IProviderStocks {

	long provide(KeyArticleClient articleClient, String magatzem);
	Map<KeyArticleClient, Stock> calculate(Set<KeyArticleClient> articles, Empresa empresa);

}

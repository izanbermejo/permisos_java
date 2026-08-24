package ames.comercial.comandes.service;

import java.util.Set;

import ames.comercial.comandes.service.ProviderReferencia.IProviderReferenciaResponse;
import ames.comercial.shared.KeyArticleClient;

public interface IProviderReferencia {

	IProviderReferenciaResponse provide(Set<KeyArticleClient> articles);
	
}

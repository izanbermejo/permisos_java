package ames.comercial.comandes.service;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import ames.comercial.advantage.internal.ObtenirReferenciaAds;
import ames.comercial.shared.KeyArticleClient;

@Component
public class ProviderReferencia implements IProviderReferencia {

	@Override
	public IProviderReferenciaResponse provide(Set<KeyArticleClient> articles) {
		return new ProviderReferenciaResponse(new ObtenirReferenciaAds().query(articles));
	}
	
	public interface IProviderReferenciaResponse {
		String referencia(KeyArticleClient key);
	}
	
	public static class ProviderReferenciaResponse implements IProviderReferenciaResponse {
		
		private Map<KeyArticleClient, String> mapReferencia;
		
		public ProviderReferenciaResponse (Map<KeyArticleClient, String> map) {
			mapReferencia = map;
		}
		
		public String referencia(KeyArticleClient key) {
			return mapReferencia.getOrDefault(key, "");
		}

	}

}

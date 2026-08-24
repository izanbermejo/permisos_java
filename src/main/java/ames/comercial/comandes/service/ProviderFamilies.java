package ames.comercial.comandes.service;

import ames.comercial.advantage.internal.IObtenirFamiliesAds;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProviderFamilies implements IProviderFamilies {

	IObtenirFamiliesAds obtenirFamiliesAds;

	public ProviderFamilies(IObtenirFamiliesAds obtenirFamiliesAds) {
		this.obtenirFamiliesAds = obtenirFamiliesAds;
	}

	@Override
	public IProviderFamiliesResponse provide(Set<KeyArticleClient> articles) {
		var setArticles = articles.stream().map(KeyArticleClient::artint).collect(Collectors.toSet());
		return new ProviderFamiliesResponse(obtenirFamiliesAds.query(setArticles));
	}
	
	public interface IProviderFamiliesResponse {
		Optional<Familia> familia(String article);
	}
	
	public static class ProviderFamiliesResponse implements IProviderFamiliesResponse {
		
		Map<String, Familia> mapFamilies;
		
		public ProviderFamiliesResponse (Map<String, Familia> mapFamilies) {
			this.mapFamilies = mapFamilies;
		}
		
		public Optional<Familia> familia(String article) {
			return Optional.ofNullable(mapFamilies.get(article));
		}

	}

}

package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.response.QueryArticleClientResponse;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.Optional;

public class ObtenirArticleClientNormalitzatsAds {

	public Optional<ObtenirArticleClientNormalitzatsAdsResponse> query(String client, KeyArticleClient keyArticleClient) {
		var optArticleClient = new ObtenirArticleClientAds().query(keyArticleClient);
		if (optArticleClient.isEmpty())
			return Optional.empty();
		// Si existeix l'articleclient i és normalitzat (clicod = 000000) cal buscar si existeix aquest articleclient
		// (pel codi de la matriu) als seus especials
		var articleClient = optArticleClient.get();
		Optional<QueryArticleClientResponse> articleClientEspecial = keyArticleClient.isNormalitzat()
									? new ObtenirArticleClientByMatriuAds().query(client, articleClient.article())
									: Optional.empty();
		return Optional.of(ObtenirArticleClientNormalitzatsAdsResponseImpl.builder()
						.articleClient(articleClient)
						.articleClientEspecial(articleClientEspecial)
						.build());
	}

	@JsonDeserialize(builder = ObtenirArticleClientNormalitzatsAdsResponseImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	public interface ObtenirArticleClientNormalitzatsAdsResponse {
		QueryArticleClientResponse articleClient();
		Optional<QueryArticleClientResponse> articleClientEspecial();
	}
			
}

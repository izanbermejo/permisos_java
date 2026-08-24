package ames.comercial.comandes.service;

import ames.comercial.advantage.internal.ObtenirPesFinalAds;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Component
public class ProviderPes implements IProviderPes {

	@Override
	public IProviderPesResponse provide(String artint) {
		return provide(Set.of(artint));
	}

	@Override
	public IProviderPesResponse provide(Set<String> articles) {
		return new ProviderPesResponse(new ObtenirPesFinalAds().query(articles));
	}
	
	public interface IProviderPesResponse {
		BigDecimal pes(String article);
		BigDecimal pes(String article, long unitats);
	}
	
	public static class ProviderPesResponse implements IProviderPesResponse {
		
		private Map<String, BigDecimal> mapPesos;
		
		public ProviderPesResponse (Map<String, BigDecimal> map) {
			mapPesos = map;
		}

		public BigDecimal pes(String article) {
			return mapPesos.getOrDefault(article, BigDecimal.ZERO);
		}

		public BigDecimal pes(String article, long unitats) {
			return mapPesos.getOrDefault(article, BigDecimal.ZERO).multiply(BigDecimal.valueOf(unitats));
		}

	}

}

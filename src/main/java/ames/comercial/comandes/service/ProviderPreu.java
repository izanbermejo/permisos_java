package ames.comercial.comandes.service;

import ames.comercial.advantage.IObtenirTipusArticleClientAds;
import ames.comercial.advantage.internal.IObtenirPreusAds;
import ames.comercial.comandes.ComandesException.*;
import ames.comercial.comandes.service.ProviderFamilies.IProviderFamiliesResponse;
import ames.comercial.shared.*;
import ames.comercial.tarifes.application.query.ObtenirPreusTarifa;
import com.google.common.collect.RangeMap;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ProviderPreu implements IProviderPreu {

	ObtenirPreusTarifa obtenirPreusTarifa;
	IObtenirPreusAds obtenirPreusAds;
	IObtenirTipusArticleClientAds obtenirTipusArticleClientAds;
	IProviderFamilies providerFamilies;

	public ProviderPreu(ObtenirPreusTarifa obtenirPreusTarifa, IObtenirPreusAds obtenirPreusAds,
						IObtenirTipusArticleClientAds obtenirTipusArticleClientAds,
						IProviderFamilies providerFamilies) {
		this.obtenirPreusTarifa = obtenirPreusTarifa;
		this.obtenirPreusAds = obtenirPreusAds;
		this.obtenirTipusArticleClientAds = obtenirTipusArticleClientAds;
		this.providerFamilies = providerFamilies;
	}

	@Override
	public IProviderPreuResponse provide(ProviderPreuRequest request) {
		var articles = request.articles();
		// Obtenció dels tipus d'articles i families
		var tipusArticles = obtenirTipusArticleClientAds.get(articles);
		var families = providerFamilies.provide(articles);

		// Construcció del map de preus que van per tarifa (tots els que no son especials)
		Map<String, RangeMap<Long, Preu>> mapPreus = new HashMap<>();
		Divisa divisa = null;

		// En cas que només es demanin articles de MARKETING posem la divisa a EUR per defecte
		var articlesMarketing = getArticlesByTipus(tipusArticles, TipusArticleClient.MARKETING);
		if (articlesMarketing.size() == request.articles().size()) {
			divisa = Divisa.EURO;
		}

		// Obtenció de tarifes de normalitzats
		var articlesNormalitzats = getArticlesByTipus(tipusArticles, TipusArticleClient.NORMALITZAT);
		if (!articlesNormalitzats.isEmpty()) {
			var optRespTarifa = obtenirPreusTarifa.executar(articlesNormalitzats, request.tarifaCoixinets().orElseThrow(TarifaCoixinetsNoDefinida::new), request.factorAplicar());
			if (optRespTarifa.isPresent()) {
				var respTarifa = optRespTarifa.get();
				divisa = checkDivisa(divisa, respTarifa.divisa());
				mapPreus.putAll(respTarifa.preus());
			}
		}

		// Obtenció de les tarifes de barres
		var articlesBarres = getArticlesBarres(articlesNormalitzats, families);
		if (!articlesBarres.isEmpty()) {
			var optRespTarifa = obtenirPreusTarifa.executar(articlesBarres, request.tarifaBarres().orElseThrow(TarifaBarresNoDefinida::new), request.factorAplicar());
			if (optRespTarifa.isPresent()) {
				var respTarifa = optRespTarifa.get();
				divisa = checkDivisa(divisa, respTarifa.divisa());
				mapPreus.putAll(respTarifa.preus());
			}
		}

		// Obtenció de tarifes d'IBINSA
		var articlesIbinsa = getArticlesByTipus(tipusArticles, TipusArticleClient.IBINSA);
		if (!articlesIbinsa.isEmpty()) {
			var optRespTarifa = obtenirPreusTarifa.executar(articlesIbinsa, request.tarifaIbinsa().orElseThrow(TarifaIbinsaNoDefinida::new), request.factorAplicar());
			if (optRespTarifa.isPresent()) {
				var respTarifa = optRespTarifa.get();
				divisa = checkDivisa(divisa, respTarifa.divisa());
				mapPreus.putAll(respTarifa.preus());
			}
		}

		// Obtenció de tarifes de MEDICAL
		var articlesMedical = getArticlesByTipus(tipusArticles, TipusArticleClient.MEDICAL);
		if (!articlesMedical.isEmpty()) {
			var optRespTarifa = obtenirPreusTarifa.executar(articlesMedical, request.tarifaMedical().orElseThrow(TarifaMedicalNoDefinida::new), request.factorAplicar());
			if (optRespTarifa.isPresent()) {
				var respTarifa = optRespTarifa.get();
				divisa = checkDivisa(divisa, respTarifa.divisa());
				mapPreus.putAll(respTarifa.preus());
			}
		}

		// Obtenció de tarifes de filtres
		var articlesFiltre = getArticlesByTipus(tipusArticles, TipusArticleClient.FILTRE);
		if (!articlesFiltre.isEmpty()) {
			var optRespTarifaBxx = obtenirPreusTarifa.executar(articlesFiltre, request.tarifaFiltresBxx().orElseThrow(TarifaFiltresNoDefinida::new), request.factorAplicar());
			if (optRespTarifaBxx.isPresent()) {
				var respTarifa = optRespTarifaBxx.get();
				divisa = checkDivisa(divisa, respTarifa.divisa());
				mapPreus.putAll(respTarifa.preus());
			}
			var optRespTarifaSsu= obtenirPreusTarifa.executar(articlesFiltre, request.tarifaFiltresSsu().orElseThrow(TarifaFiltresNoDefinida::new), request.factorAplicar());
			if (optRespTarifaSsu.isPresent()) {
				var respTarifa = optRespTarifaSsu.get();
				divisa = checkDivisa(divisa, respTarifa.divisa());
				mapPreus.putAll(respTarifa.preus());
			}
			var optRespTarifaSxx= obtenirPreusTarifa.executar(articlesFiltre, request.tarifaFiltresSxx().orElseThrow(TarifaFiltresNoDefinida::new), request.factorAplicar());
			if (optRespTarifaSxx.isPresent()) {
				var respTarifa = optRespTarifaSxx.get();
				divisa = checkDivisa(divisa, respTarifa.divisa());
				mapPreus.putAll(respTarifa.preus());
			}
			var optRespTarifaSsuPlaquest= obtenirPreusTarifa.executar(articlesFiltre, request.tarifaFiltresSsuPlaques().orElseThrow(TarifaFiltresNoDefinida::new), request.factorAplicar());
			if (optRespTarifaSsuPlaquest.isPresent()) {
				var respTarifa = optRespTarifaSsuPlaquest.get();
				divisa = checkDivisa(divisa, respTarifa.divisa());
				mapPreus.putAll(respTarifa.preus());
			}
		}

		// Obtenció dels preus dels especials
		var articlesEspecials = getArticlesByTipus(tipusArticles, TipusArticleClient.ESPECIAL);
		Map<KeyArticleClient, Preu> preusEspecials = new HashMap<>();
		if (!articlesEspecials.isEmpty()) {
			preusEspecials = obtenirPreusAds.query(articlesEspecials);
			Map<Divisa, Long> mapDivises = preusEspecials.values().stream()
					.collect(Collectors.groupingBy(p -> p.divisa().base(), Collectors.counting()));
			// Si hi ha mes d'una entrada al map vol dir que hi han divises diferents entre els articles
			if (mapDivises.size() > 1)
				throw new PesesEspecialsDiferentDivisa();
			var divisaEspecials = mapDivises.keySet().stream().findFirst().orElse(null);
			divisa = checkDivisa(divisa, divisaEspecials);
		}

		return new ProviderPreuResponse(tipusArticles, families, divisa, mapPreus, preusEspecials);
	}

	private Divisa checkDivisa(Divisa actual, Divisa nova) {
		if (actual == null)
			return nova;
		if (!actual.equals(nova))
			throw new DivisaTarifesDiferent();
		return actual;
	}

	private Set<KeyArticleClient> getArticlesByTipus(Map<KeyArticleClient, TipusArticleClient> map, TipusArticleClient tipus) {
		return map.entrySet().stream()
				.filter(e -> tipus.equals(e.getValue()))
				.map(Entry::getKey)
				.collect(Collectors.toSet());
	}

	private Set<KeyArticleClient> getArticlesBarres(Set<KeyArticleClient> articles, IProviderFamiliesResponse families) {
		return articles.stream()
				.filter(a -> families.familia(a.artint()).map(f -> f.isBarra).orElse(false))
				.collect(Collectors.toSet());
	}

	public interface IProviderPreuResponse {
		Optional<Preu> preu(KeyArticleClient article, long unitats);
		Optional<TipusArticleClient> tipus(KeyArticleClient article);
		Optional<Divisa> divisa();
		Optional<RangeMap<Long, Preu>> rang(KeyArticleClient articleClient);
	}
	
	public static class ProviderPreuResponse implements IProviderPreuResponse {

		Map<KeyArticleClient, TipusArticleClient> mapTipusArticleClient;
		IProviderFamiliesResponse families;
		Optional<Divisa> divisa;
		Map<String, RangeMap<Long, Preu>> mapPreus;
		Map<KeyArticleClient, Preu> mapPreusEspecials;
		
		public ProviderPreuResponse (Map<KeyArticleClient, TipusArticleClient> mapTipusArticleClient,
									 IProviderFamiliesResponse families,
									 Divisa divisa,
									 Map<String, RangeMap<Long, Preu>> mapPreus,
									 Map<KeyArticleClient, Preu> mapPreusEspecials) {
			this.mapTipusArticleClient = mapTipusArticleClient;
			this.families = families;
			this.divisa = Optional.ofNullable(divisa);
			this.mapPreus = mapPreus;
			this.mapPreusEspecials = mapPreusEspecials;
		}
		
		public Optional<Preu> preu(KeyArticleClient articleClient, long unitats) {
			var tipus = mapTipusArticleClient.get(articleClient);
			if (tipus == TipusArticleClient.ESPECIAL)
				return preuEspecial(articleClient);
			return preuNormalitzat(articleClient.artint(), unitats);
		}

		@Override
		public Optional<TipusArticleClient> tipus(KeyArticleClient article) {
			return Optional.ofNullable(mapTipusArticleClient.get(article));
		}

		@Override
		public Optional<Divisa> divisa() {
			return divisa;
		}

		private Optional<Preu> preuNormalitzat(String artint, long unitats){
			var preus = mapPreus.get(artint);
			if (preus == null)
				return Optional.empty();
			return Optional.ofNullable(preus.get(unitats));
		}

		private Optional<Preu> preuEspecial(KeyArticleClient articleClient){
			return Optional.ofNullable(mapPreusEspecials.get(articleClient));
		}

//		calcula els preus per pram segons un articleClient
		@Override
		public Optional<RangeMap<Long, Preu>> rang(KeyArticleClient articleClient) {
			var preus = mapPreus.get(articleClient.artint());
			if (preus == null)
				return Optional.empty();
			return Optional.of(preus);
		}

	}

}

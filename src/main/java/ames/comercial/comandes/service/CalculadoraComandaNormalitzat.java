package ames.comercial.comandes.service;

import ames.comercial.advantage.IObtenirTipusArticleClientAds;
import ames.comercial.advantage.internal.ObtenirClientAds;
import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import ames.comercial.calculadorapreus.request.CalculadoraPreusReq.LiniaCalculPreuReq;
import ames.comercial.calculadorapreus.request.CalculadoraPreusReqImpl;
import ames.comercial.calculadorapreus.request.LiniaCalculPreuReqImpl;
import ames.comercial.calculadorapreus.service.CalculadoraPreus;
import ames.comercial.comandes.ComandesException.DivisaClientDiferentTarifes;
import ames.comercial.comandes.ComandesException.PreuNoCalculat;
import ames.comercial.comandes.ComandesException.QuantitatNoServibleSegonsTarifa;
import ames.comercial.comandes.internal.domain.linia.Reservable;
import ames.comercial.comandes.service.ICalculPotServirLiniaNormalitzat.CalculServibleResp;
import ames.comercial.comandes.service.request.LiniaNormalitzatReq;
import ames.comercial.comandes.service.request.LiniaNormalitzatReqImpl;
import ames.comercial.comandes.service.request.TarifesCalculadoraReq;
import ames.comercial.comandes.service.response.CalculComandaNormalitzatResponse;
import ames.comercial.comandes.service.response.CalculComandaNormalitzatResponseImpl;
import ames.comercial.comandes.service.response.LiniaNormalitzatResp;
import ames.comercial.comandes.service.response.LiniaNormalitzatRespImpl;
import ames.comercial.costtransport.ICalcularCostTransport;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Numbers;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import ames.comercial.shared.TipusArticleClient;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CalculadoraComandaNormalitzat {

	IProviderPreu providerPreu;
	IProviderFamilies providerFamilies;
	IProviderReferencia providerReferencia;
	IProviderPes providerPes;
	ICalcularCostTransport calcularCostTransport;
	IProviderStocks providerStocks;
	IProviderDiesReserva providerDiesReserva;
	IObtenirTipusArticleClientAds obtenirTipusArticleClient;
	IProviderTarifaActual obtenirTarifaActual;
	
	public CalculadoraComandaNormalitzat(IProviderPreu providerPreu,
										 IProviderFamilies providerFamilies,
										 IProviderReferencia providerReferencia,
										 IProviderPes providerPes,
										 ICalcularCostTransport calcularCostTransport,
										 IProviderStocks providerStocks,
										 IProviderDiesReserva providerDiesReserva,
										 IObtenirTipusArticleClientAds obtenirTipusArticleClient,
										 IProviderTarifaActual obtenirTarifaActual) {
		this.providerPreu = providerPreu;
		this.providerFamilies = providerFamilies;
		this.providerReferencia = providerReferencia;
		this.providerPes = providerPes;
		this.calcularCostTransport = calcularCostTransport;
		this.providerStocks = providerStocks;
		this.providerDiesReserva = providerDiesReserva;
		this.obtenirTipusArticleClient = obtenirTipusArticleClient;
		this.obtenirTarifaActual = obtenirTarifaActual;
	}

	public CalculComandaNormalitzatResponse calcula (String codiClient, List<LiniaNormalitzatReq> linies) {
		// Obtenció del client
		var client = new ObtenirClientAds().get(codiClient).orElseThrow(() -> new ClientNoExisteix(codiClient));
		// Empresa del client
		var empresa = Empresa.getByClau(client.empresa());
		var tarifesActualClient = obtenirTarifaActual.executar(codiClient);
		return calcula(client, empresa, TarifesCalculadoraReq.from(tarifesActualClient), linies);
	}

	public CalculComandaNormalitzatResponse calcula (String codiClient, TarifesCalculadoraReq tarifesCalcul, List<LiniaNormalitzatReq> linies) {
		// Obtenció del client
		var client = new ObtenirClientAds().get(codiClient).orElseThrow(() -> new ClientNoExisteix(codiClient));
		// Empresa del client
		var empresa = Empresa.getByClau(client.empresa());
		return calcula(client, empresa, tarifesCalcul, linies);
	}

	public CalculComandaNormalitzatResponse calcula (ClientAds client, Empresa empresa, TarifesCalculadoraReq tarifesCalcul, List<LiniaNormalitzatReq> liniesReq) {
		if (liniesReq == null || liniesReq.isEmpty())
			return CalculComandaNormalitzatResponse.empty();

		// Normalització de la request per posar identificador de linia a aquelles que no ho tenen
		// (això passa quan s'afegeix una línia des del client)
		var liniesNorm = new ArrayList<LiniaNormalitzatReq>();
		var maxLinia = liniesReq.stream().mapToLong(LiniaNormalitzatReq::linia).max().orElse(0);
		var offSetLinies = 1;
		for (var l : liniesReq) {
			liniesNorm.add(LiniaNormalitzatReqImpl.builder().from(l)
					.linia(l.linia() > 0 ? l.linia() : maxLinia+offSetLinies++)
					.build());
		}
		
		// Agrupació de les peces per peça i quantitat
		Map<KeyArticleClient, Long> pesesAgrupades = liniesNorm
				.stream()
				.collect(Collectors.groupingBy(LiniaNormalitzatReq::articleClient,
						Collectors.summingLong(LiniaNormalitzatReq::quantitat)));
		// Conjunt d'articles
		var conjuntArticles = pesesAgrupades
								.keySet()
								.stream()
								.map(KeyArticleClient::artint)
								.collect(Collectors.toSet());
		// Obtenció dels tipus de peça
		var mapTipusPesa = obtenirTipusArticleClient.get(pesesAgrupades.keySet());

		// Càlcul dels preus
		var calcPreus = new CalculadoraPreus(providerPreu, providerFamilies, mapTipusPesa, client.dtoCoixBronze(), client.dtoCoixFerro(), client.dtoFiltres());
		var liniesPreus = calcPreus.calcul(CalculadoraPreusReqImpl.builder()
						.client(client.clicod())
						.divisa(client.divisa())
						.tarifaCoixinets(tarifesCalcul.coixinets())
						.tarifaBarres(tarifesCalcul.barres())
						.tarifaMedical(tarifesCalcul.medical())
						.tarifaIbinsa(tarifesCalcul.ibinsa())
						.tarifaFiltresBxx(tarifesCalcul.filtresBxx())
						.tarifaFiltresSsu(tarifesCalcul.filtresSsu())
						.tarifaFiltresSxx(tarifesCalcul.filtresSxx())
						.tarifaFiltresSsuPlaques(tarifesCalcul.filtresSsuPlaques())
						.linies(buildRequestLiniesPreus(liniesNorm))
						.build());

		// Cal comprovar que la divisa del client és la mateixa que la del càlcul de preus
		if (!client.divisa().equals(liniesPreus.divisa())) {
			throw new DivisaClientDiferentTarifes(client.divisa(), liniesPreus.divisa());
		}

		// Obtenció de les referencies
		var refResp = providerReferencia.provide(pesesAgrupades.keySet());
		// Obtenció dels stocks
		var stocks = providerStocks.calculate(pesesAgrupades.keySet(), empresa);
		// Obtenció dels dies d'antel·lació de la reserva
		var diesAntelacioReserva = providerDiesReserva.provide();
		
		// Obtenció de les línies reservables (només en les que s'ha demanat calcular les reserves)
		var liniesCalCalcularResrva = liniesNorm.stream().filter(LiniaNormalitzatReq::isCalcularReserves).toList();
		var liniesReservables = new CalculPotServirLiniaNormalitzat(stocks, mapTipusPesa, diesAntelacioReserva)
								.calcul(liniesCalCalcularResrva);
		
		// Obtenció dels pesos
		var pesosResp = providerPes.provide(conjuntArticles);
		
		// Construcció del resultat
		var resultat = new ArrayList<LiniaNormalitzatResp>();
		for (var l : liniesNorm) {
			var optReserva = Optional.ofNullable(liniesReservables.get(l.linia()));
			var preuResp = liniesPreus.linies().stream().filter(lp -> lp.linia() == l.linia()).findAny().orElseThrow(() -> new PreuNoCalculat(l.linia()));
			var referencia = refResp.referencia(l.articleClient());
			// En cas que el preu obtingut sigui negatiu vol dir que no es pot vendre aquesta quantitat segons la tarifa
			if (Numbers.isSmall(preuResp.preu().valor()).than(0)) {
				throw new QuantitatNoServibleSegonsTarifa(l.quantitat(), referencia);
			}
			// Construcció de la línia resultatn
			var tipusArticle = mapTipusPesa.get(l.articleClient());
			resultat.add(LiniaNormalitzatRespImpl.builder()
					.linia(l.linia())
					.articleClient(l.articleClient())
					.referencia(referencia)
					.tipusArticleClient(tipusArticle)
					.quantitat(l.quantitat())
					.dataSolicitada(l.dataSolicitada())
					.dataPrevistaSortida(l.dataPrevistaSortida())
					.preu(l.preuFixat().orElse(preuResp.preu()))
					.isPreuFixat(l.preuFixat().isPresent())
					.descompte(preuResp.descompte())
					.pes(pesosResp.pes(l.articleClient().artint(), l.quantitat()))
					.quantitatCalcul(quantitatCalcul(tipusArticle, l.preuFixat().isPresent(), pesesAgrupades.getOrDefault(l.articleClient(), 0L)))
					.reservable(optReserva.map(CalculServibleResp::reservable).orElse(Reservable.NO_APLICA))
					.quantitatReservable(optReserva.map(CalculServibleResp::quantitatReserva).orElse(0L))
					.stockDisponible(optReserva.map(CalculServibleResp::stockDisponible).orElse(0L))
					.build());
		}

		// Càlcul del cost de transport
		var pesKg = CalculadoraUtils.pesKg(resultat);
		var importTotal = LiniaNormalitzatResp.importNet(resultat);
		var costTransport = calcularCostTransport.calcula(client.clicod(), importTotal, pesKg, client.incoterm());

		// Ordenació de les línies per data i número
		resultat.sort(Comparator.comparing(LiniaNormalitzatResp::dataSolicitada)
				.thenComparing(Comparator.comparing(LiniaNormalitzatResp::linia)));

		return CalculComandaNormalitzatResponseImpl.builder()
				.tarifaCoixinets(tarifesCalcul.coixinets())
				.tarifaBarres(tarifesCalcul.barres())
				.tarifaMedical(tarifesCalcul.medical())
				.tarifaIbinsa(tarifesCalcul.ibinsa())
				.tarifaFiltresBxx(tarifesCalcul.filtresBxx())
				.tarifaFiltresSsu(tarifesCalcul.filtresSsu())
				.tarifaFiltresSxx(tarifesCalcul.filtresSxx())
				.tarifaFiltresSsuPlaques(tarifesCalcul.filtresSsuPlaques())
				.addAllLinies(resultat)
				.costTransport(costTransport)
				.build();
	}

	private List<LiniaCalculPreuReq> buildRequestLiniesPreus (List<LiniaNormalitzatReq> linies) {
		var result = new ArrayList<LiniaCalculPreuReq>();
		for (var l : linies) {
			result.add(LiniaCalculPreuReqImpl.builder()
							.linia(l.linia())
							.articleClient(l.articleClient())
							.quantitat(l.quantitat())
							.build());
		}
		return result;
	}

	private long quantitatCalcul(TipusArticleClient tipusArticleClient, boolean isPreuFixat, long quantitatPeses) {
		if (TipusArticleClient.ESPECIAL.equals(tipusArticleClient))
			return 0L;
		if (isPreuFixat)
			return 0L;
		return quantitatPeses;
	}
	
}

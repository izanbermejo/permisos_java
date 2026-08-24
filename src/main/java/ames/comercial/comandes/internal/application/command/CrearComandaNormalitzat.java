package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.IObtenirNumeradorComanda;
import ames.comercial.advantage.internal.ObtenirClientAds;
import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import ames.comercial.comandes.events.ComandaNormalitzatCreadaEvent;
import ames.comercial.comandes.internal.domain.comanda.*;
import ames.comercial.comandes.internal.domain.linia.*;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.internal.service.ICalcularServible;
import ames.comercial.comandes.request.CrearComandaNormalitzatRequest;
import ames.comercial.comandes.request.CrearComandaNormalitzatResponse;
import ames.comercial.comandes.request.CrearComandaNormalitzatResponseImpl;
import ames.comercial.comandes.service.CalculadoraComandaNormalitzat;
import ames.comercial.comandes.service.response.CalculComandaNormalitzatResponse;
import ames.comercial.comandes.service.response.LiniaNormalitzatResp;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.InformacioEnviamentImpl;
import ames.comercial.shared.Numbers;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import ames.comercial.shared.SimpleItem;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CrearComandaNormalitzat {
	
	ApplicationEventPublisher applicationEventPublisher;
	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaComandaRepo;
	CalculadoraComandaNormalitzat calculadoraComanda;
	ICalcularServible calcularServible;
	IObtenirNumeradorComanda obtenirNumeradorComanda;
	
	public CrearComandaNormalitzat(ComandaRepository comandaRepo, LiniaComandaRepository liniaComandaRepo, 
			CalculadoraComandaNormalitzat calculadoraComanda, ICalcularServible calcularServible,
			IObtenirNumeradorComanda obtenirNumeradorComanda,
			ApplicationEventPublisher applicationEventPublisher) {
		this.comandaRepo = comandaRepo;
		this.liniaComandaRepo = liniaComandaRepo;
		this.calculadoraComanda = calculadoraComanda;
		this.calcularServible = calcularServible;
		this.obtenirNumeradorComanda = obtenirNumeradorComanda;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Transactional
	public CrearComandaNormalitzatResponse executar (CrearComandaNormalitzatRequest req) {
		// Obtenció del client
		var client = new ObtenirClientAds().get(req.codiClient()).orElseThrow(() -> new ClientNoExisteix(req.codiClient()));

		// Càlcul de la comanda
		var resp = calculadoraComanda.calcula(req.codiClient(), req.linies());
		
		// Creació de la comanda i de les línies
		long idComanda = obtenirNumeradorComanda.obtenir();
		var linies = linies(idComanda, resp.linies());
		var comanda = buildComanda(idComanda, req, client, resp, linies);
		// Save de la comanda i de les linies
		comandaRepo.save(comanda);
		liniaComandaRepo.save(linies);
		
		applicationEventPublisher.publishEvent(new ComandaNormalitzatCreadaEvent(this, comanda, linies));

		// Càlcul de si la comanda supera els 1.000€ i es tracta d'un distribuidor nacional
		var avisComandaDistribuidorNacional = client.isNacional()
				&& client.isDistribuidor()
				&& Numbers.isHighEq(comanda.dadesNormalitzat().orElseThrow().importNet()).than(1_000);
		return CrearComandaNormalitzatResponseImpl.builder()
				.comanda(idComanda)
				.avisComandaSuperiorDistribuidorNacional(avisComandaDistribuidorNacional)
				.build();
	}
	
	private Comanda buildComanda(long idComanda, CrearComandaNormalitzatRequest req,
								 ClientAds client, CalculComandaNormalitzatResponse resp,
								 List<LiniaComanda> linies) {
		ComandaProps props = ComandaPropsImpl.builder()
				.tipus(TipusComanda.NORMALITZAT)
				.dades(buildDades(client))
				.informacioClient(buildInfoClient(req))
				.adresa(client.adresaEnviament().orElse(client.adresa()))
				.informacioEnviament(buildInformacioEnviament(client))
				.servida(false)
				.usuari(RequestThread.nomUsuari().toUpperCase())
				.servible(calcularServible.calcula(linies))
				.dadesNormalitzat(DadesNormalitzatImpl.builder()
						.importNet(resp.importNet())
						.importBrut(resp.importBrut())
						.divisa(resp.divisa().orElseThrow())
						.pes(resp.pes())
						.costTransport(resp.costTransport().orElse(BigDecimal.ZERO))
						.tarifes(DadesNormalitzatTarifesImpl.builder()
								.tarifaCoixinets(resp.tarifaCoixinets())
								.tarifaBarres(resp.tarifaBarres())
								.tarifaIbinsa(resp.tarifaIbinsa())
								.tarifaMedical(resp.tarifaMedical())
								.tarifaFiltresBxx(resp.tarifaFiltresBxx())
								.tarifaFiltresSxx(resp.tarifaFiltresSxx())
								.tarifaFiltresSsu(resp.tarifaFiltresSsu())
								.tarifaFiltresSsuPlaques(resp.tarifaFiltresSsuPlaques())
								.build())
						.build())
				.build();
		return new Comanda(idComanda, props);
	}
	
	private DadesComanda buildDades(ClientAds client) {
		return DadesComandaImpl.builder()
				.client(client.clicod())
				.clientNom(client.nom())
				.empresa(client.empresa())
				.dataAlta(RequestThread.dateLocal())
				.build();
	}

	private InformacioEnviament buildInformacioEnviament(ClientAds client) {
		return InformacioEnviamentImpl.builder()
				.formaEnviament(client.formaEnviament())
				.incoterm(client.incoterm())
				.desti(client.desti())
				.transportista(client.codiTransportista())
				.zonaTransport(client.zonaTransport().map(SimpleItem::codi).orElse(""))
				.build();
	}
	
	private InformacioClient buildInfoClient(CrearComandaNormalitzatRequest req) {
		return InformacioClientImpl.builder()
				.identificador(req.comanda())
				.data(req.dataRecepcio())
				.programa("")
				.build();
	}
	
	private List<LiniaComanda> linies(long idComanda, List<LiniaNormalitzatResp> value) {
		var result = new ArrayList<LiniaComanda>();
		for (var l : value) {
			result.add(LiniaComandaImpl.builder()
					.id(KeyLiniaComanda.of(idComanda, l.linia()))
					.articleClient(l.articleClient())
					.tipusArticleClient(l.tipusArticleClient())
					.referencia(l.referencia())
					.tipus(TipusLiniaComanda.FERM)
					.quantitat(l.quantitat())
					.preu(l.preu())
					.dataCreacio(LocalDateTime.now())
					.dataSolicitada(l.dataSolicitada())
					.dataPrevistaSortida(l.dataPrevistaSortida())
					.quantitatServida(0L)
					.reserva(InformacioReservaImpl.builder()
							.estat(l.reservable())
							.quantitat(l.quantitatReservable())
							.build())
					.dadesCalcul(DadesCalculNormalitzatImpl.builder()
							.descompte(l.descompte())
							.quantitatCalcul(l.quantitatCalcul())
							.build())
					.build());
		}
		return result;
	}
	
}

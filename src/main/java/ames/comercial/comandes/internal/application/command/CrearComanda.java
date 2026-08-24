package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.IObtenirNumeradorComanda;
import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import ames.comercial.comandes.internal.domain.comanda.*;
import ames.comercial.comandes.internal.domain.linia.InformacioReserva;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComandaImpl;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.request.CrearComandaRequest;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.*;
import ames.comercial.shared.SharedExceptions.ArticleClientNotFound;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CrearComanda {

	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaRepo;
	IObtenirNumeradorComanda obtenirNumeradorComanda;
	IObtenirClientAds obtenirClientAds;
	IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda;
	
	public CrearComanda(ComandaRepository comandaRepo, LiniaComandaRepository liniaRepo,
						IObtenirNumeradorComanda obtenirNumeradorComanda, IObtenirClientAds obtenirClientAds,
						IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda) {
		this.comandaRepo = comandaRepo;
		this.liniaRepo = liniaRepo;
		this.obtenirNumeradorComanda = obtenirNumeradorComanda;
		this.obtenirClientAds = obtenirClientAds;
		this.obtenirArticleClientInformacioComanda = obtenirArticleClientInformacioComanda;
	}

	@Transactional
	public String executar (CrearComandaRequest req) {
		// Obtenció de l'articleclient a partir dels 13 dígits
		var articleClient = obtenirArticleClientInformacioComanda.executar(req.articleClient()).orElseThrow(() -> new ArticleClientNotFound(req.articleClient()));
		// Creació de la clau articleclient
		var clauArticleClient = KeyArticleClient.of(articleClient.artint(), articleClient.codiClient());
		// Obtenció del client
		var client = obtenirClientAds.get(clauArticleClient.clicod()).orElseThrow(() -> new ClientNoExisteix(clauArticleClient.clicod()));

		// Es crea una comanda o s'afegeix a una si ja existeix prèviament
		var optComandaExistent = comandaRepo.findByComandaClient(clauArticleClient.clicod(), req.comanda(), Empresa.getByClau(client.empresa()));
		Comanda comanda = optComandaExistent.orElseGet(() -> buildNewComanda(req, client));
		var codiComanda = comanda.codi();

		// Creació de la 1a línia
		var linia = buildLinia(codiComanda, clauArticleClient, articleClient.referencia(), req);
		liniaRepo.save(linia);

		return linia.codiNumeroFormat();
	}
	
	private Comanda buildNewComanda(CrearComandaRequest req, ClientAds client) {
		var newCodiComanda = obtenirNumeradorComanda.obtenir();
		ComandaProps props = ComandaPropsImpl.builder()
				.tipus(TipusComanda.PROGRAMA)
				.dades(buildDades(req, client))
				.informacioClient(buildInfoClient(req))
				.adresa(client.adresaEnviament().orElse(client.adresa()))
				.informacioEnviament(buildInformacioEnviament(client))
				.servida(false)
				.servible(Servible.NO_APLICA)
				.usuari(RequestThread.nomUsuari())
				.build();
		var comanda = new Comanda(newCodiComanda, props);
		comandaRepo.save(comanda);
		return comanda;
	}
	
	private DadesComanda buildDades(CrearComandaRequest req, ClientAds client) {
		return DadesComandaImpl.builder()
				.client(client.clicod())
				.clientNom(client.nom())
				.empresa(client.empresa())
				.dataAlta(RequestThread.dateLocal())
				.build();
	}
	
	private InformacioClient buildInfoClient(CrearComandaRequest req) {
		return InformacioClientImpl.builder()
				.identificador(req.comanda())
				.data(RequestThread.dateLocal())
				.programa(req.programa())
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

	private LiniaComanda buildLinia(long codiComanda, KeyArticleClient clauArticleClient, String referencia, CrearComandaRequest req) {
		var numLinia = liniaRepo.nextNumero(codiComanda);
		return LiniaComandaImpl.builder()
				.id(KeyLiniaComanda.of(codiComanda, numLinia))
				.articleClient(clauArticleClient)
				.tipusArticleClient(TipusArticleClient.ESPECIAL)
				.tipus(req.tipus())
				.quantitat(req.quantitat())
				.preu(Preu.of(req.preu(), Divisa.getBySymbol(req.divisa())))
				.isPreuFixat(req.isPreuFixat())
				.dataSolicitada(req.dataSolicitada())
				.dataPrevistaSortida(req.dataPrevistaSortida())
				.dataPrevistaSortidaInterna(req.dataPrevistaSortidaInterna())
				.dataConfirmadaFabrica(req.dataConfirmadaFabrica())
				.reserva(InformacioReserva.empty())
				.quantitatServida(0)
				.dataCreacio(LocalDateTime.now())
				.referencia(referencia)
				.comandaBlanca(req.comandaBlanca())
				.build();
	}
	
}

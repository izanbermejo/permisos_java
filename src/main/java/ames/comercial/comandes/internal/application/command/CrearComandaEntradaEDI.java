package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.IObtenirNumeradorComanda;
import ames.comercial.advantage.internal.ObtenirArticleClientAds;
import ames.comercial.advantage.internal.ObtenirClientAds;
import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import ames.comercial.comandes.ext.IProcessarLiniesComanda.ProcessarLiniesComandaReq;
import ames.comercial.comandes.internal.domain.comanda.*;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.request.LiniaComandaRequest;
import ames.comercial.comandes.request.LiniaComandaRequestImpl;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.InformacioEnviamentImpl;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import ames.comercial.shared.SimpleItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CrearComandaEntradaEDI {

	ComandaRepository comandaRepo;
	CrearLiniaComanda crearLiniaComanda;
	IObtenirNumeradorComanda obtenirNumeradorComanda;

	public CrearComandaEntradaEDI(ComandaRepository comandaRepo, CrearLiniaComanda crearLiniaComanda,
								  IObtenirNumeradorComanda obtenirNumeradorComanda) {
		this.comandaRepo = comandaRepo;
		this.crearLiniaComanda = crearLiniaComanda;
		this.obtenirNumeradorComanda = obtenirNumeradorComanda;
	}

	@Transactional
	public long executar (KeyArticleClient articleClient, ProcessarLiniesComandaReq procLinia) {
		// creació de la comanda
		long idComanda = obtenirNumeradorComanda.obtenir();
		var comanda = buildComanda(idComanda, articleClient.clicod(), procLinia);
		comandaRepo.save(comanda);
		// Creació de la línia
		crearLiniaComanda.executar(idComanda, buildRequest(articleClient, procLinia));
		return idComanda;
	}

	private LiniaComandaRequest buildRequest(KeyArticleClient articleClient, ProcessarLiniesComandaReq procLinia) {
		var artCli = new ObtenirArticleClientAds().query(articleClient).orElseThrow();
		return LiniaComandaRequestImpl.builder()
				.quantitat(procLinia.quantitat())
				.articleClient(articleClient)
				.dataSolicitada(procLinia.dataSolicitada())
				.dataPrevistaSortida(procLinia.dataPrevistaSortida())
				.dataPrevistaSortidaInterna(procLinia.dataPrevistaSortidaInterna())
				.tipus(procLinia.tipus())
				.preu(artCli.preu())
				.divisa(artCli.divisa())
				.isPreuFixat(false)
				.build();
	}
	
	private Comanda buildComanda(long idComanda, String client, ProcessarLiniesComandaReq req) {
		var clientAds = new ObtenirClientAds().get(client).orElseThrow(() -> new ClientNoExisteix(client));
		ComandaProps props = ComandaPropsImpl.builder()
				.tipus(TipusComanda.PROGRAMA)
				.dades(buildDades(clientAds))
				.informacioClient(buildInfoClient(req.comandaClient()))
				.adresa(clientAds.adresaEnviament().orElse(clientAds.adresa()))
				.informacioEnviament(buildInfoEnviament(clientAds))
				.servida(false)
				.servible(Servible.NO_APLICA)
				.usuari(RequestThread.nomUsuari())
				.build();
		return new Comanda(idComanda, props);
	}
	
	private DadesComanda buildDades(ClientAds cliAds) {
		return DadesComandaImpl.builder()
				.client(cliAds.clicod())
				.clientNom(cliAds.nom())
				.empresa(cliAds.empresa())
				.dataAlta(RequestThread.dateLocal())
				.build();
	}
	
	private InformacioClient buildInfoClient(String comandaClient) {
		return InformacioClientImpl.builder()
				.identificador(comandaClient)
				.data(RequestThread.dateLocal())
				.programa("")
				.build();
	}

	private InformacioEnviament buildInfoEnviament(ClientAds cliAds) {
		return InformacioEnviamentImpl.builder()
				.formaEnviament(cliAds.formaEnviament())
				.incoterm(cliAds.incoterm())
				.desti(cliAds.desti())
				.transportista(cliAds.codiTransportista())
				.zonaTransport(cliAds.zonaTransport().map(SimpleItem::codi))
				.build();
	}
	
}

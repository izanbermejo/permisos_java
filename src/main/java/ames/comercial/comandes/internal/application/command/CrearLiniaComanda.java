package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.internal.ObtenirArticleClientAds;
import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.ComandesException.OperacioNoPermesaComandaStockSeguretat;
import ames.comercial.comandes.internal.domain.linia.InformacioReserva;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComandaImpl;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.request.LiniaComandaRequest;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.Preu;
import ames.comercial.shared.TipusArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CrearLiniaComanda {

	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaRepo;
	ActualitzarEstatComanda actualitzarEstatComanda;
	@Autowired ActualitzarComentarisClientLiniaComanda actialitzarComentsClient;
	@Autowired ActualitzarComentarisInternsLiniaComanda actialitzarComentsIntern;

	public CrearLiniaComanda (ComandaRepository comandaRepo, LiniaComandaRepository liniaRepo, ActualitzarEstatComanda actualitzarEstatComanda) {
		this.comandaRepo = comandaRepo;
		this.liniaRepo = liniaRepo;
		this.actualitzarEstatComanda = actualitzarEstatComanda;
	}

	@Transactional
	public String executar (long codiComanda, LiniaComandaRequest req) {
		// Obtenció de la comanda
		var comanda = comandaRepo.find(codiComanda).orElseThrow(ComandaNoExisteix::new);
		// No es pot crear una línia de comanda en una comanda d'stock de seguretat
		if (comanda.isStockSeguretat())
			throw new OperacioNoPermesaComandaStockSeguretat();

		// Obtenció de la referència
		var ref = new ObtenirArticleClientAds().query(req.articleClient()).orElseThrow();
		// Creació de la línia
		var numLinia = liniaRepo.nextNumero(codiComanda);
		var linia = LiniaComandaImpl.builder()
				.id(KeyLiniaComanda.of(codiComanda, numLinia))
				.articleClient(req.articleClient())
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
				.referencia(ref.referencia())
				.comandaBlanca(req.comandaBlanca())
				.build();
		liniaRepo.save(linia);
		// Actualització de l'estat de la comanda
		actualitzarEstatComanda.executar(comanda.codi());
		if (req.comentarisClient().isPresent()) actialitzarComentsClient.executar(linia.id(), req.comentarisClient().get());
		if (req.comentarisInterns().isPresent()) actialitzarComentsIntern.executar(linia.id(), req.comentarisInterns().get());
		return linia.codiNumeroFormat();
	}
	
}

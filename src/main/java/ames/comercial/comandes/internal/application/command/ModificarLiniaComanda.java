package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.ComandesException.LiniaComandaNoExisteix;
import ames.comercial.comandes.ComandesException.QuantitatNoPotSerInferiorServida;
import ames.comercial.comandes.internal.domain.linia.LiniaComandaImpl;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import ames.comercial.comandes.request.LiniaComandaRequest;
import ames.comercial.shared.Preu;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModificarLiniaComanda {
	
	ApplicationEventPublisher applicationEventPublisher;
	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaRepo;
	ActualitzarEstatComanda actualitzarEstatComanda;
	
	public ModificarLiniaComanda (ApplicationEventPublisher applicationEventPublisher, ComandaRepository comandaRepo,
			LiniaComandaRepository liniaRepo, ActualitzarEstatComanda actualitzarEstatComanda) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.comandaRepo = comandaRepo;
		this.liniaRepo = liniaRepo;
		this.actualitzarEstatComanda = actualitzarEstatComanda;
	}

	@Transactional
	public String executar (long codiComanda, long numLinia, LiniaComandaRequest req) {
		var comanda = comandaRepo.find(codiComanda).orElseThrow(ComandaNoExisteix::new);
		var linia = liniaRepo.find(codiComanda, numLinia).orElseThrow(LiniaComandaNoExisteix::new);

		if (req.quantitat() < linia.quantitatServida())
			throw new QuantitatNoPotSerInferiorServida(linia.quantitatServida());

		var newLinia = LiniaComandaImpl.builder()
				.from(linia)
				.tipus(req.tipus())
				.quantitat(req.quantitat())
				.preu(Preu.of(req.preu(), req.divisa()))
				.isPreuFixat(req.isPreuFixat())
				.dataSolicitada(req.dataSolicitada())
				.dataPrevistaSortida(req.dataPrevistaSortida())
				.dataPrevistaSortidaInterna(req.dataPrevistaSortidaInterna())
				.dataConfirmadaFabrica(req.dataConfirmadaFabrica())
				.comandaBlanca(req.comandaBlanca())
				.build();
		// En cas que la línia no hagi canviat no es fa res
		if (newLinia.equals(linia))
			return newLinia.codiNumeroFormat();
		// Es guarda la línia i s'emet l'event de línia modificada
		liniaRepo.save(newLinia);

		// Actualització de l'estat de la comanda
		actualitzarEstatComanda.executar(comanda.codi());

		return newLinia.codiNumeroFormat();
	}
	
}

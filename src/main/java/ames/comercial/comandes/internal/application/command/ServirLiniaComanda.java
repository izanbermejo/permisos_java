package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.ComandesException.LiniaComandaNoExisteix;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServirLiniaComanda {
	
	ApplicationEventPublisher applicationEventPublisher;
	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaRepo;
	
	public ServirLiniaComanda (ApplicationEventPublisher applicationEventPublisher, ComandaRepository comandaRepo,
			LiniaComandaRepository liniaRepo) {
		this.applicationEventPublisher = applicationEventPublisher;
		this.comandaRepo = comandaRepo;
		this.liniaRepo = liniaRepo;
	}

	@Transactional
	public String executar(KeyLiniaComanda liniaComanda, long quantitat) {
		var codiComanda = liniaComanda.comanda();
		var numLinia = liniaComanda.numero();
		// Obtenció de la comanda i de la línia
		var comanda = comandaRepo.find(codiComanda).orElseThrow(ComandaNoExisteix::new);
		var linia = liniaRepo.find(codiComanda, numLinia).orElseThrow(LiniaComandaNoExisteix::new);
		var newLinia = linia.servir(quantitat);
		liniaRepo.save(newLinia);
		return newLinia.codiNumeroFormat();
	}
	
}

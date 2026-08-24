package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.LiniaComandaNoExisteix;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CanviarFermOrientatiuLiniaComanda {

	ApplicationEventPublisher applicationEventPublisher;
	LiniaComandaRepository liniaComandaRepo;

	public CanviarFermOrientatiuLiniaComanda(LiniaComandaRepository liniaComandaRepo,
                                             ApplicationEventPublisher applicationEventPublisher) {
		this.liniaComandaRepo = liniaComandaRepo;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Transactional
	public void executar (KeyLiniaComanda clauLinia) {
		// Obtenció de la línia
		var linia = liniaComandaRepo.find(clauLinia).orElseThrow(LiniaComandaNoExisteix::new);
		var newLinia = linia.canviarFermOrientatiu();
		liniaComandaRepo.save(newLinia);
	}
	
}

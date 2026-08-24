package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ActualitzarEstatComanda {

	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaComandaRepo;
	
	public ActualitzarEstatComanda(ComandaRepository comandaRepo, LiniaComandaRepository liniaComandaRepo) {
		this.comandaRepo = comandaRepo;
		this.liniaComandaRepo = liniaComandaRepo;
	}

	@Transactional
	public void executar (long codi) {
		// Obtenció de la quantitat pendent de servir de la comanda
		var comanda = comandaRepo.find(codi).orElseThrow(ComandaNoExisteix::new);
		var quantitatPendent = liniaComandaRepo.quantitatPendent(codi);
		if (quantitatPendent > 0) {
			comanda.marcarPendent();
		} else {
			comanda.marcarServida();
		}
		comandaRepo.save(comanda);
	}

}

package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.IObtenirNumeradorComanda;
import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.ComandesException.LiniaComandaNoExisteix;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.LiniaComandaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnularLiniaComanda {

	ComandaRepository comandaRepo;
	LiniaComandaRepository liniaComandaRepo;
	IObtenirNumeradorComanda obtenirNumeradorComanda;
	ActualitzarEstatComanda actualitzarEstatComanda;

	public AnularLiniaComanda(ComandaRepository comandaRepo, LiniaComandaRepository liniaComandaRepo,
                              IObtenirNumeradorComanda obtenirNumeradorComanda,
							  ActualitzarEstatComanda actualitzarEstatComanda) {
		this.comandaRepo = comandaRepo;
		this.liniaComandaRepo = liniaComandaRepo;
		this.obtenirNumeradorComanda = obtenirNumeradorComanda;
		this.actualitzarEstatComanda = actualitzarEstatComanda;
	}

	@Transactional
	public void executar (KeyLiniaComanda keyLinia) {
		// Obtenció de la comanda
		var comanda = comandaRepo.find(keyLinia.comanda()).orElseThrow(ComandaNoExisteix::new);
		// Obtenicó de la línia a anul·lar
		var linia = liniaComandaRepo.find(keyLinia.comanda(), keyLinia.numero()).orElseThrow(LiniaComandaNoExisteix::new);
		// En cas que la línia estigui servida no cal fer res
		if (linia.servida())
			return;
		// S'anul·la la línia
		var liniaAnulada = linia.anular();
		liniaComandaRepo.save(liniaAnulada);
		// Pot ser que hagi canviat l'estat de la comanda
		actualitzarEstatComanda.executar(comanda.codi());
	}
	
}

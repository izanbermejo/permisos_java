package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.ComandesException.AdjuntNotFound;
import ames.comercial.comandes.internal.application.query.ObtenirNomFitxerDisponible;
import ames.comercial.comandes.internal.infraestructure.adjunt.AdjuntComandaRepositoryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RenombrarAdjuntComanda {

	@Autowired ObtenirNomFitxerDisponible obtenirNomFitxerDisponible;
	@Autowired AdjuntComandaRepositoryDatabase repositoryDatabase;
	
	@Transactional
	public void executar(long comanda, String codiFitxer, String nomFitxer) {
		var adjuntActual = repositoryDatabase.find(comanda, codiFitxer).orElseThrow(AdjuntNotFound::new);
		// En cas que tingui el mateix que actualment nom no cal fer res
		if (adjuntActual.nom().equals(nomFitxer))
			return;
		// Obtenció del nom disponible
		String nomFinal = obtenirNomFitxerDisponible.executar(comanda, nomFitxer);
		var nouAdjunt = adjuntActual.canviarNom(nomFinal);
		repositoryDatabase.add(codiFitxer, nouAdjunt);
	}

}

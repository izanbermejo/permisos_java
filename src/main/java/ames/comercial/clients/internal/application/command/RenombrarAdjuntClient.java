package ames.comercial.clients.internal.application.command;

import ames.comercial.clients.internal.application.query.ObtenirNomFitxerDisponibleClient;
import ames.comercial.clients.internal.infraestructure.adjunt.AdjuntClientRepositoryDatabase;
import ames.comercial.clients.ClientsException.AdjuntNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RenombrarAdjuntClient {

	@Autowired
	ObtenirNomFitxerDisponibleClient obtenirNomFitxerDisponible;
	@Autowired
	AdjuntClientRepositoryDatabase repositoryDatabase;
	
	@Transactional
	public void executar(String codiClient, String codiFitxer, String nomFitxer) {
		var adjuntActual = repositoryDatabase.find(codiClient, codiFitxer).orElseThrow(AdjuntNotFound::new);
		// En cas que tingui el mateix que actualment nom no cal fer res
		if (adjuntActual.nom().equals(nomFitxer))
			return;
		// Obtenció del nom disponible
		String nomFinal = obtenirNomFitxerDisponible.executar(codiClient, nomFitxer);
		var nouAdjunt = adjuntActual.canviarNom(nomFinal);
		repositoryDatabase.add(codiFitxer, nouAdjunt);
	}

}

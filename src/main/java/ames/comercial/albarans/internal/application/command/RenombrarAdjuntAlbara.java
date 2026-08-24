package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.internal.application.query.ObtenirNomFitxerDisponibleAlbara;
import ames.comercial.albarans.internal.infraestructure.adjunt.AdjuntAlbaraRepositoryDatabase;
import ames.comercial.comandes.ComandesException.AdjuntNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RenombrarAdjuntAlbara {

	@Autowired
	ObtenirNomFitxerDisponibleAlbara obtenirNomFitxerDisponible;
	@Autowired AdjuntAlbaraRepositoryDatabase repositoryDatabase;
	
	@Transactional
	public void executar(long albara, String codiFitxer, String nomFitxer) {
		var adjuntActual = repositoryDatabase.find(albara, codiFitxer).orElseThrow(AdjuntNotFound::new);
		// En cas que tingui el mateix que actualment nom no cal fer res
		if (adjuntActual.nom().equals(nomFitxer))
			return;
		// Obtenció del nom disponible
		String nomFinal = obtenirNomFitxerDisponible.executar(albara, nomFitxer);
		var nouAdjunt = adjuntActual.canviarNom(nomFinal);
		repositoryDatabase.add(codiFitxer, nouAdjunt);
	}

}

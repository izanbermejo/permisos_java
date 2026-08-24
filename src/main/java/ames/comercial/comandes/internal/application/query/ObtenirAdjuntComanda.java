package ames.comercial.comandes.internal.application.query;

import ames.comercial.comandes.ComandesException.AdjuntNotFound;
import ames.comercial.comandes.internal.domain.Adjunt;
import ames.comercial.comandes.internal.infraestructure.adjunt.AdjuntComandaRepositoryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObtenirAdjuntComanda {
	
	@Autowired AdjuntComandaRepositoryDatabase adjuntRepository;
	
	public Adjunt executar(long comanda, String codiFitxer) {
		return adjuntRepository.find(comanda, codiFitxer).orElseThrow(AdjuntNotFound::new);
	}
	
}

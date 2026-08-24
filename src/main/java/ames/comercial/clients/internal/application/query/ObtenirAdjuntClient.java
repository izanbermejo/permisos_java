package ames.comercial.clients.internal.application.query;

import ames.comercial.clients.internal.infraestructure.adjunt.AdjuntClientRepositoryDatabase;
import ames.comercial.clients.ClientsException.AdjuntNotFound;
import ames.comercial.clients.internal.domain.Adjunt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObtenirAdjuntClient {
	
	@Autowired
	AdjuntClientRepositoryDatabase adjuntRepository;
	
	public Adjunt executar(String codiClient, String codiFitxer) {
		return adjuntRepository.find(codiClient, codiFitxer).orElseThrow(AdjuntNotFound::new);
	}
	
}

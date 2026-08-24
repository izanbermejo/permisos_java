package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.internal.infraestructure.adjunt.AdjuntAlbaraRepositoryDatabase;
import ames.comercial.comandes.ComandesException.AdjuntNotFound;
import ames.comercial.albarans.internal.domain.Adjunt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ObtenirAdjuntAlbara {
	
	@Autowired AdjuntAlbaraRepositoryDatabase adjuntRepository;
	
	public Adjunt executar(long comanda, String codiFitxer) {
		return adjuntRepository.find(comanda, codiFitxer).orElseThrow(AdjuntNotFound::new);
	}
	
}

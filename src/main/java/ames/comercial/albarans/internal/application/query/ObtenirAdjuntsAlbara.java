package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.internal.domain.Adjunt;
import ames.comercial.albarans.internal.infraestructure.adjunt.AdjuntAlbaraRepositoryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

;

@Component
public class ObtenirAdjuntsAlbara {
	
	@Autowired
	AdjuntAlbaraRepositoryDatabase adjuntRepository;
	
	public List<Adjunt> executar(long comanda) {
		return adjuntRepository.findByComanda(comanda);
	}
	
}

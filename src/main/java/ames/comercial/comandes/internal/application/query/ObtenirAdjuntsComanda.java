package ames.comercial.comandes.internal.application.query;

import ames.comercial.comandes.internal.domain.Adjunt;
import ames.comercial.comandes.internal.infraestructure.adjunt.AdjuntComandaRepositoryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirAdjuntsComanda {
	
	@Autowired
	AdjuntComandaRepositoryDatabase adjuntRepository;
	
	public List<Adjunt> executar(long comanda) {
		return adjuntRepository.findByComanda(comanda);
	}
	
}

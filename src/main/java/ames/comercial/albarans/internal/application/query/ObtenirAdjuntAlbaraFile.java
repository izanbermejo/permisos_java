package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.internal.infraestructure.adjunt.AdjuntAlbaraRepository;
import ames.comercial.comandes.ComandesException.AdjuntNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ObtenirAdjuntAlbaraFile {

	@Autowired AdjuntAlbaraRepository repositoryDisc;

	public File executar(long comanda, String codiFitxer) {
		return repositoryDisc.find(comanda, codiFitxer).orElseThrow(AdjuntNotFound::new);
	}
	
}

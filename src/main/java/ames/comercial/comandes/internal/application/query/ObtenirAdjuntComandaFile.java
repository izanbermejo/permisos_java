package ames.comercial.comandes.internal.application.query;

import ames.comercial.comandes.ComandesException.AdjuntNotFound;
import ames.comercial.comandes.internal.infraestructure.adjunt.AdjuntComandaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ObtenirAdjuntComandaFile {

	@Autowired AdjuntComandaRepository repositoryDisc;

	public File executar(long comanda, String codiFitxer) {
		return repositoryDisc.find(comanda, codiFitxer).orElseThrow(AdjuntNotFound::new);
	}
	
}

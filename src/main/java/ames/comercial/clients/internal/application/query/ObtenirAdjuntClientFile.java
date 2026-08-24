package ames.comercial.clients.internal.application.query;

import ames.comercial.clients.ClientsException.AdjuntNotFound;
import ames.comercial.clients.internal.infraestructure.adjunt.AdjuntClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class ObtenirAdjuntClientFile {

	@Autowired AdjuntClientRepository repositoryDisc;

	public File executar(String codiClient, String codiFitxer) {
		return repositoryDisc.find(codiClient, codiFitxer).orElseThrow(AdjuntNotFound::new);
	}
	
}

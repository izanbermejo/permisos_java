package ames.comercial.clients.internal.application.command;

import ames.comercial.clients.internal.application.query.ObtenirNomFitxerDisponibleClient;
import ames.comercial.clients.internal.domain.Adjunt;
import ames.comercial.clients.internal.domain.AdjuntImpl;
import ames.comercial.clients.internal.domain.Categoria;
import ames.comercial.clients.internal.infraestructure.adjunt.AdjuntClientRepository;
import ames.comercial.clients.internal.infraestructure.adjunt.AdjuntClientRepositoryDatabase;
import ames.comercial.server.RequestThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

@Component
public class AfegirAdjuntClient {

	@Autowired ApplicationEventPublisher eventPublisher;
	@Autowired
	ObtenirNomFitxerDisponibleClient obtenirNomFitxerDisponibleClient;
	@Autowired AdjuntClientRepository repositoryDisc;
	@Autowired AdjuntClientRepositoryDatabase repositoryDatabase;
	
	@Transactional
	public void executar(String codiClient, long categoria, InputStream inputStream, String nomFitxer) {
		String nomFinal = obtenirNomFitxerDisponibleClient.executar(codiClient, nomFitxer);
		// Codi del fitxer i extensió
		String extension = extension(nomFinal);
		String codiFitxer = random(extension);
		// Emmagatzematge a la BBDD
		Adjunt a = AdjuntImpl.builder()
				.client(codiClient)
				.codiFitxer(codiFitxer)
				.nom(nomFinal)
				.categoria(Categoria.getById(categoria))
				.usuari(RequestThread.nomUsuari())
				.data(RequestThread.dateTimeLocal())
				.build();
		repositoryDatabase.add(codiFitxer, a);
		// Emmagatzematge de l'adjunt al disc (si falla es farà rollback de la transacció)
		repositoryDisc.add(codiClient, inputStream, codiFitxer);
	}
	
	private String extension (String nomFitxer) {
		String extension = "";
		int i = nomFitxer.lastIndexOf('.');
		if (i > 0) {
		    extension = nomFitxer.substring(i+1);
		}
		return extension;
	}
	
	private String random (String extension) {
		String uuid = UUID.randomUUID().toString().replace("-", "").replace(" ", "");
		return new Random().ints(10, 0, uuid.length())
    	.mapToObj(pos -> uuid.charAt(pos))
    	.map(c -> String.valueOf(c))
    	.reduce("", String::concat) + "." + extension
    	.trim();
	}
	
}

package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.events.AdjuntAfegitComandaEvent;
import ames.comercial.comandes.internal.application.query.ObtenirNomFitxerDisponible;
import ames.comercial.comandes.internal.domain.Adjunt;
import ames.comercial.comandes.internal.domain.AdjuntImpl;
import ames.comercial.comandes.internal.infraestructure.adjunt.AdjuntComandaRepository;
import ames.comercial.comandes.internal.infraestructure.adjunt.AdjuntComandaRepositoryDatabase;
import ames.comercial.server.RequestThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Component
public class AfegirAdjuntComanda {

	@Autowired ApplicationEventPublisher eventPublisher;
	@Autowired ObtenirNomFitxerDisponible obtenirNomFitxerDisponible;
	@Autowired AdjuntComandaRepository repositoryDisc;
	@Autowired AdjuntComandaRepositoryDatabase repositoryDatabase;
	
	@Transactional
	public void executar(long comanda, InputStream inputStream, String nomFitxer) {
		String nomFinal = obtenirNomFitxerDisponible.executar(comanda, nomFitxer);
		// Codi del fitxer i extensió
		String extension = extension(nomFinal);
		String codiFitxer = random(extension);
		// Emmagatzematge a la BBDD
		Adjunt a = AdjuntImpl.builder()
				.comanda(comanda)
				.codiFitxer(codiFitxer)
				.nom(nomFinal)
				.usuari(RequestThread.nomUsuari())
				.data(RequestThread.dateTimeLocal())
				.build();
		repositoryDatabase.add(codiFitxer, a);
		// Emmagatzematge de l'adjunt al disc (si falla es farà rollback de la transacció)
		repositoryDisc.add(comanda, inputStream, codiFitxer);
		// Publicació de l'event (actualització del número d'adjunts de la comanda)
		eventPublisher.publishEvent(new AdjuntAfegitComandaEvent(this, comanda));
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

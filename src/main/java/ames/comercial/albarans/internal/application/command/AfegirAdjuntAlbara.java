package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.internal.application.query.ObtenirNomFitxerDisponibleAlbara;
import ames.comercial.albarans.internal.domain.Adjunt;
import ames.comercial.albarans.internal.domain.AdjuntImpl;
import ames.comercial.albarans.internal.infraestructure.adjunt.AdjuntAlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.adjunt.AdjuntAlbaraRepositoryDatabase;
import ames.comercial.server.RequestThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Random;
import java.util.UUID;

@Component
public class AfegirAdjuntAlbara {

	@Autowired
	ObtenirNomFitxerDisponibleAlbara obtenirNomFitxerDisponible;
	@Autowired AdjuntAlbaraRepository repositoryDisc;
	@Autowired AdjuntAlbaraRepositoryDatabase repositoryDatabase;
	
	@Transactional
	public void executar(long albara, InputStream inputStream, String nomFitxer) {
		String nomFinal = obtenirNomFitxerDisponible.executar(albara, nomFitxer);
		// Codi del fitxer i extensió
		String extension = extension(nomFinal);
		String codiFitxer = random(extension);
		// Emmagatzematge a la BBDD
		Adjunt a = AdjuntImpl.builder()
				.albara(albara)
				.codiFitxer(codiFitxer)
				.nom(nomFinal)
				.usuari(RequestThread.nomUsuari())
				.data(RequestThread.dateTimeLocal())
				.build();
		repositoryDatabase.add(codiFitxer, a);
		// Emmagatzematge de l'adjunt al disc (si falla es farà rollback de la transacció)
		repositoryDisc.add(albara, inputStream, codiFitxer);
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

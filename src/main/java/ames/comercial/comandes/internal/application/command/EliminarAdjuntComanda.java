package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.events.AdjuntEliminatComandaEvent;
import ames.comercial.comandes.internal.infraestructure.adjunt.AdjuntComandaRepository;
import ames.comercial.comandes.internal.infraestructure.adjunt.AdjuntComandaRepositoryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EliminarAdjuntComanda {
	
	@Autowired AdjuntComandaRepository repositoryDatabase;
	@Autowired AdjuntComandaRepositoryDatabase repositoryDisc;
	@Autowired ApplicationEventPublisher eventPublisher;
	
	@Transactional
	public void executar(long comanda, String codiAdjunt) {
		repositoryDisc.remove(comanda, codiAdjunt);
		repositoryDatabase.remove(comanda, codiAdjunt);
		eventPublisher.publishEvent(new AdjuntEliminatComandaEvent(this, comanda));
	}
	
}

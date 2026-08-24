package ames.comercial.clients.internal.application.command;

import ames.comercial.clients.internal.infraestructure.adjunt.AdjuntClientRepository;
import ames.comercial.clients.internal.infraestructure.adjunt.AdjuntClientRepositoryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EliminarAdjuntClient {
	
	@Autowired AdjuntClientRepositoryDatabase repositoryDatabase;
	@Autowired AdjuntClientRepository repositoryDisc;
	@Autowired ApplicationEventPublisher eventPublisher;
	
	@Transactional
	public void executar(String client, String codiAdjunt) {
		repositoryDisc.remove(client, codiAdjunt);
		repositoryDatabase.remove(client, codiAdjunt);
	}
	
}

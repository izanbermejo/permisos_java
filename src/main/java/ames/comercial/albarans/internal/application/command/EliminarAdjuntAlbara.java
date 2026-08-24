package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.internal.infraestructure.adjunt.AdjuntAlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.adjunt.AdjuntAlbaraRepositoryDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

;

@Component
public class EliminarAdjuntAlbara {
	
	@Autowired AdjuntAlbaraRepository repositoryDatabase;
	@Autowired AdjuntAlbaraRepositoryDatabase repositoryDisc;

	@Transactional
	public void executar(long albara, String codiAdjunt) {
		repositoryDisc.remove(albara, codiAdjunt);
		repositoryDatabase.remove(albara, codiAdjunt);
	}
	
}

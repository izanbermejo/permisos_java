package ames.comercial.comandes.listeners;

import ames.comercial.comandes.events.AdjuntAfegitComandaEvent;
import ames.comercial.comandes.events.AdjuntEliminatComandaEvent;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AdjuntComandaAfegitEliminatListener {

	@Autowired ComandaRepository comandaRepo;

	@EventListener
	public void onApplicationEvent(AdjuntAfegitComandaEvent event) {
		comandaRepo.updateNumAdjunts(event.comanda());
	}

	@EventListener
	public void onApplicationEvent(AdjuntEliminatComandaEvent event) {
		comandaRepo.updateNumAdjunts(event.comanda());
	}

}

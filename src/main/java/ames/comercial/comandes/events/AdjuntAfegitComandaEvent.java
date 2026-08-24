package ames.comercial.comandes.events;

import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class AdjuntAfegitComandaEvent extends ApplicationEvent {

	long comanda;

	public AdjuntAfegitComandaEvent(Object source, long comanda) {
		super(source);
		this.comanda = comanda;
	}

	public long comanda() {
		return this.comanda;
	}
	
}

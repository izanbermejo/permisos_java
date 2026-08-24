package ames.comercial.comandes.events;

import org.springframework.context.ApplicationEvent;

@SuppressWarnings("serial")
public class AdjuntEliminatComandaEvent extends ApplicationEvent {

	long comanda;

	public AdjuntEliminatComandaEvent(Object source, long comanda) {
		super(source);
		this.comanda = comanda;
	}

	public long comanda() {
		return this.comanda;
	}
	
}

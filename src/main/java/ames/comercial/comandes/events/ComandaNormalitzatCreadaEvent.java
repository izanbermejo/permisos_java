package ames.comercial.comandes.events;

import ames.comercial.comandes.internal.domain.comanda.Comanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.shared.Empresa;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@SuppressWarnings("serial")
public class ComandaNormalitzatCreadaEvent extends ApplicationEvent {

	Comanda comanda;
	List<LiniaComanda> linies;
	Empresa empresa;

	public ComandaNormalitzatCreadaEvent(Object source, Comanda comanda, List<LiniaComanda> linies) {
		super(source);
		this.comanda = comanda;
		this.linies = linies;
		this.empresa = Empresa.getByClau(comanda.dades().empresa());
	}

	public List<LiniaComanda> linies() {
		return this.linies;
	}
	
	public Comanda comanda() {
		return this.comanda;
	}

	public Empresa empresa() { return  this.empresa; }

}

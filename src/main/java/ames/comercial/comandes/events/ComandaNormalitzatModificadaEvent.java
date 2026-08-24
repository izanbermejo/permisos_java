package ames.comercial.comandes.events;

import ames.comercial.comandes.internal.domain.comanda.Comanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.context.ApplicationEvent;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("serial")
public class ComandaNormalitzatModificadaEvent extends ApplicationEvent {

	Comanda comanda;
	List<LiniaComanda> linies;
	Optional<KeyArticleClient> articleClient;
	Optional<Long> incrementReserva;

	public ComandaNormalitzatModificadaEvent(Object source, Comanda comanda, List<LiniaComanda> linies) {
		this (source, comanda, linies, null, null);
	}

	public ComandaNormalitzatModificadaEvent(Object source, Comanda comanda, List<LiniaComanda> linies,
											 KeyArticleClient articleClient, Long incrementReserva) {
		super(source);
		this.comanda = comanda;
		this.linies = linies;
		this.articleClient = Optional.ofNullable(articleClient);
		this.incrementReserva = Optional.ofNullable(incrementReserva);
	}

	public Comanda comanda() {
		return this.comanda;
	}

	public List<LiniaComanda> linies() { return this.linies; }

	public KeyArticleClient articleClient() { return this.articleClient.orElseThrow(); }

	public long incrementReserva() {
		return incrementReserva.orElse(0L);
	}

}

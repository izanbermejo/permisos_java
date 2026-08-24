package ames.comercial.comandes.listeners;

import ames.comercial.comandes.events.ComandaNormalitzatModificadaEvent;
import ames.comercial.comandes.internal.application.command.ActualitzarEstatComanda;
import ames.comercial.comandes.internal.application.command.RecaculcarServibleComanda;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component("ComandaNormalitzatModificadaListenerComandes")
public class ComandaNormalitzatModificadaListener {

	RecaculcarServibleComanda recaculcarServibleComanda;
	ActualitzarEstatComanda actualitzarEstatComanda;

	public ComandaNormalitzatModificadaListener(RecaculcarServibleComanda recaculcarServibleComanda,
												ActualitzarEstatComanda actualitzarEstatComanda) {
		this.recaculcarServibleComanda = recaculcarServibleComanda;
		this.actualitzarEstatComanda = actualitzarEstatComanda;
	}

	@EventListener
	@Order(Integer.MIN_VALUE)
	public void onApplicationEvent(ComandaNormalitzatModificadaEvent event) {
		// Conjunt d'identificadors on ha pogut canviar el seu estat
		Set<Long> codisComanda = Stream.concat(
				Stream.of(event.comanda().codi()),
				event.linies().stream()
						.map(l -> l.id().comanda())
		).collect(Collectors.toSet());
		// Per cada identificador s'ha de recalcular si és servible
		codisComanda.forEach(recaculcarServibleComanda::executar);
		// Actualització de l'estat de la comanda
		actualitzarEstatComanda.executar(event.comanda().codi());
	}

}

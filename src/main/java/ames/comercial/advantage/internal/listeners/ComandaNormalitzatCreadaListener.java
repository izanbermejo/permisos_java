package ames.comercial.advantage.internal.listeners;

import ames.comercial.comandes.events.ComandaNormalitzatCreadaEvent;
import ames.comercial.comandes.internal.domain.comanda.Comanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.inventari.ext.IIncrementarReservaArticleclient;
import ames.comercial.shared.Empresa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ComandaNormalitzatCreadaListener {

	@Autowired IIncrementarReservaArticleclient incrementarReservaArticleclient;

	@EventListener
	public void onApplicationEvent(ComandaNormalitzatCreadaEvent event) {
		Comanda comanda = event.comanda();
		List<LiniaComanda> linies = event.linies();
		Empresa empresa = event.empresa();

		// UPDATE stocks
		linies.stream()
				.filter(l -> l.tipusArticleClient().isSistemaReserva())
				.collect(Collectors.toMap(LiniaComanda::articleClient, LiniaComanda::quantitatReservada, Long::sum))
				.forEach((articleClient, quantitatReservada) -> incrementarReservaArticleclient.executar(articleClient, empresa, quantitatReservada));
	}

}

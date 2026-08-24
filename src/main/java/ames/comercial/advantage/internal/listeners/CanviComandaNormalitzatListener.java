package ames.comercial.advantage.internal.listeners;

import ames.comercial.comandes.events.ComandaNormalitzatModificadaEvent;
import ames.comercial.comandes.internal.domain.comanda.Comanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.inventari.ext.IIncrementarReservaArticleclient;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CanviComandaNormalitzatListener {

	@Autowired IIncrementarReservaArticleclient incrementarReservaArticleclient;

	/**
	 * Listener de l'event que s'executa quan es modifica una comanda de normalitzats.
	 * S'actualitza la quantitat reservada en el cas de normalitzats
	 *
	 * @param event {@link ComandaNormalitzatModificadaEvent} amb la informació de l'event produït
	 */
	@EventListener
	public void onApplicationEvent(ComandaNormalitzatModificadaEvent event) {
		// Obtenció de dades de l'event
		Comanda comanda = event.comanda();
		List<LiniaComanda> linies = event.linies();
		long incrementReserva = event.incrementReserva();
		Empresa empresa = Empresa.getByClau(comanda.dades().empresa());

		// En cas que hagi increment de reserva
		if (incrementReserva > 0) {
			KeyArticleClient articleClient = event.articleClient();
			incrementarReservaArticleclient.executar(articleClient, empresa, incrementReserva);
		}
	}

}

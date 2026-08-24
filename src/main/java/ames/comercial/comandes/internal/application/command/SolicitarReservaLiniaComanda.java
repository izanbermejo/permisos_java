package ames.comercial.comandes.internal.application.command;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.service.IRecalculReservesArticleclient;
import ames.comercial.comandes.internal.domain.service.RecalculReservesArticleclient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolicitarReservaLiniaComanda {

	IRecalculReservesArticleclient recalculReservesArticleclient;

	public SolicitarReservaLiniaComanda(RecalculReservesArticleclient recalculReservesArticleclient) {
		this.recalculReservesArticleclient = recalculReservesArticleclient;
	}

	@Transactional
	public void executar (KeyLiniaComanda liniaComanda) {
		recalculReservesArticleclient.executar(liniaComanda);
	}

}

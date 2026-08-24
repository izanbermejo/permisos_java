package ames.comercial.comandes.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ProviderDiesReserva implements IProviderDiesReserva {

	@Override
	public long provide() {
		// TODO Suministrar informació a tráves de la taula setup de l'Advantage
		return 30;
	}

	@Override
	public LocalDate dataMaxReserva() {
		return LocalDate.now().plusDays(provide());
	}


}

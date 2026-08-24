package ames.comercial.comandes.service;

import java.time.LocalDate;

public interface IProviderDiesReserva {

	long provide();
	LocalDate dataMaxReserva();
	
}

package ames.comercial.comandes.service;

import ames.comercial.comandes.internal.domain.linia.Reservable;
import ames.comercial.comandes.service.request.LiniaNormalitzatReq;

import java.util.HashMap;
import java.util.List;

public interface ICalculPotServirLiniaNormalitzat {

	HashMap<Long, CalculServibleResp> calcul(List<LiniaNormalitzatReq> linies);

	record CalculServibleResp (Reservable reservable, long quantitatReserva, long stockDisponible) {};

}
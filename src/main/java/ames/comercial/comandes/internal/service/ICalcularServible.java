package ames.comercial.comandes.internal.service;

import ames.comercial.comandes.internal.domain.comanda.Servible;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;

import java.util.List;

public interface ICalcularServible {

    Servible calcula (List<LiniaComanda> linies);

}

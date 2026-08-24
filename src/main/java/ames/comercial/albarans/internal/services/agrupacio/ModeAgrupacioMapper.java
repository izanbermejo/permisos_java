package ames.comercial.albarans.internal.services.agrupacio;

import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;

public class ModeAgrupacioMapper {

    public static ModeAgrupacio from(ClientAds client) {
        if (client.isAlbaraPerComanda() && client.isAlbaraPerPesa()) {
            return ModeAgrupacio.AGRUPAR_PER_COMANDA_PESA;
        } else if (client.isAlbaraPerComanda()) {
            return ModeAgrupacio.AGRUPAR_PER_COMANDA;
        } else if (client.isAlbaraPerPesa()) {
            return ModeAgrupacio.AGRUPAR_PER_PESA;
        } else {
            return ModeAgrupacio.AGRUPAR_TOT;
        }
    }

}

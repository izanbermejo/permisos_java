package ames.comercial.albarans.internal.services.agrupacio;

import ames.comercial.albarans.internal.services.agrupacio.strategies.AgruparPerComanda;
import ames.comercial.albarans.internal.services.agrupacio.strategies.AgruparPerComandaPesa;
import ames.comercial.albarans.internal.services.agrupacio.strategies.AgruparPerPesa;
import ames.comercial.albarans.internal.services.agrupacio.strategies.AgruparTot;
import org.springframework.stereotype.Component;

@Component
public class AgrupadorFactory {

    public AgrupadorAlbara get(ModeAgrupacio mode) {
        return switch (mode) {
            case AGRUPAR_TOT -> new AgruparTot();
            case AGRUPAR_PER_COMANDA -> new AgruparPerComanda();
            case AGRUPAR_PER_PESA -> new AgruparPerPesa();
            case AGRUPAR_PER_COMANDA_PESA -> new AgruparPerComandaPesa();
        };
    }

}

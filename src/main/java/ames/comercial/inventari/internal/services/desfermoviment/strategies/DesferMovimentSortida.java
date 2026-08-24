package ames.comercial.inventari.internal.services.desfermoviment.strategies;

import ames.comercial.comandes.internal.application.command.DesferLiniaComanda;
import ames.comercial.inventari.internal.domain.moviment.Moviment;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import ames.comercial.inventari.internal.services.desfermoviment.EstrategiaDesferMoviment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Estratègia de desfer per als moviments de SORTIDA: desfà la línia de comanda associada. Tota sortida
 * prové d'una línia de comanda (també els consums, que serveixen la comanda pendent FIFO).
 */
@Component
public class DesferMovimentSortida implements EstrategiaDesferMoviment {

    @Autowired DesferLiniaComanda desferLiniaComanda;

    @Override
    public TipusMoviment tipus() {
        return TipusMoviment.SORTIDA;
    }

    @Override
    public void desfer(Moviment moviment) {
        var sortida = moviment.sortida().orElseThrow();
        desferLiniaComanda.executar(sortida.liniaComanda(), moviment.quantitat());
    }

}

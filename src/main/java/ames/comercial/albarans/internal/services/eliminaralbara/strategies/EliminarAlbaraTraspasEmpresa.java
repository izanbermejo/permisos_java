package ames.comercial.albarans.internal.services.eliminaralbara.strategies;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.services.eliminaralbara.EstrategiaEliminarAlbara;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Estratègia d'eliminació per als traspassos amb canvi d'empresa: si el traspàs és abonable, cada línia
 * té un pendent d'abonar a {@code penabo} de l'Advantage que desapareix amb ella.
 * <p>
 * Només cobreix els tipus amb canvi d'empresa perquè són els únics que es poden marcar com a abonables
 * (veure {@link Albara#checkPotCanviarTraspasAbonable}); als altres traspassos no hi ha res a desfer.
 */
@Component
public class EliminarAlbaraTraspasEmpresa implements EstrategiaEliminarAlbara {

    @Override
    public List<TipusAlbara> tipus() {
        return List.of(TipusAlbara.TRASPAS_EMPRESA, TipusAlbara.TRASPAS_MAGATZEM_EMPRESA);
    }

    @Override
    public void desferAlbara(Albara albara, List<LiniaAlbara> linies) {
        if (!albara.isTraspasAbonable()) return;
        linies.forEach(l -> ReplicaAdvantage.instance().addPenAboDelete(albara, l));
    }

    @Override
    public void desferLinia(Albara albara, LiniaAlbara linia) {
        if (!albara.isTraspasAbonable()) return;
        ReplicaAdvantage.instance().addPenAboDelete(albara, linia);
    }

}

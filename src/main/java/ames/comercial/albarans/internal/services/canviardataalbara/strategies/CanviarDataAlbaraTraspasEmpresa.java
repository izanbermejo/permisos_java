package ames.comercial.albarans.internal.services.canviardataalbara.strategies;

import ames.comercial.advantage.ReplicaAdvantage;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.services.canviardataalbara.EstrategiaCanviarDataAlbara;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Propagació per als traspassos amb canvi d'empresa: si el traspàs és abonable, la data forma part de la
 * clau dels pendents d'abonar a {@code penabo} de l'Advantage, de manera que s'han de reescriure
 * esborrant-los amb la data antiga i refent-los amb la nova.
 * <p>
 * Refer-los torna a deixar el pendent d'abonar igual a la quantitat de la línia, cosa que només és
 * correcta perquè la data només es pot canviar amb l'albarà obert (ho garanteix
 * {@link Albara#checkPotCanviarData}) i un albarà obert encara no pot tenir cap abonament començat.
 * <p>
 * Només cobreix els tipus amb canvi d'empresa perquè són els únics que es poden marcar com a abonables.
 */
@Component
public class CanviarDataAlbaraTraspasEmpresa implements EstrategiaCanviarDataAlbara {

    @Override
    public List<TipusAlbara> tipus() {
        return List.of(TipusAlbara.TRASPAS_EMPRESA, TipusAlbara.TRASPAS_MAGATZEM_EMPRESA);
    }

    @Override
    public void propagar(Albara albaraAnterior, Albara albaraNou, List<LiniaAlbara> linies) {
        if (!albaraNou.isTraspasAbonable()) return;
        linies.forEach(l -> {
            ReplicaAdvantage.instance().addPenAboDelete(albaraAnterior, l);
            ReplicaAdvantage.instance().addPenAboInsert(albaraNou, l);
        });
    }

}

package ames.comercial.albarans.internal.services.eliminaralbara.strategies;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.services.DesferConsumPlataforma;
import ames.comercial.albarans.internal.services.eliminaralbara.EstrategiaEliminarAlbara;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Estratègia d'eliminació per als albarans de consum: restableix el pendent de consumir a les línies
 * dels albarans de traspàs a plataforma i esborra la traçabilitat de {@code sortides_plataforma}.
 */
@Component
public class EliminarAlbaraConsum implements EstrategiaEliminarAlbara {

    @Autowired DesferConsumPlataforma desferConsumPlataforma;

    @Override
    public List<TipusAlbara> tipus() {
        return List.of(TipusAlbara.CONSUM);
    }

    @Override
    public void desferAlbara(Albara albara, List<LiniaAlbara> linies) {
        desferConsumPlataforma.executarPerAlbara(albara.id());
    }

    @Override
    public void desferLinia(Albara albara, LiniaAlbara linia) {
        desferConsumPlataforma.executarPerLinia(linia.id());
    }

}

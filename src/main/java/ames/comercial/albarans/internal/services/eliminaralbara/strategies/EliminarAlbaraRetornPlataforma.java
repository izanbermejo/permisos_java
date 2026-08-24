package ames.comercial.albarans.internal.services.eliminaralbara.strategies;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.services.DesferConsumPlataforma;
import ames.comercial.albarans.internal.services.RegistrarRetornPlataforma;
import ames.comercial.albarans.internal.services.eliminaralbara.EstrategiaEliminarAlbara;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Estratègia d'eliminació per als traspassos que poden ser un retorn de mercaderia d'una plataforma
 * del SII ({@link RegistrarRetornPlataforma}): restableix el pendent de consumir a les línies dels
 * albarans de traspàs a plataforma i esborra la traçabilitat de {@code sortides_plataforma}, igual que
 * en els albarans de consum.
 * <p>
 * Només cobreix {@code TRASPAS_MAGATZEM}, que és l'únic tipus que pot ser un retorn: la mercaderia que
 * hi ha a la plataforma continua sent de l'empresa que la hi va enviar, de manera que tornar-la no
 * canvia mai d'empresa. No es comprova si l'albarà era realment un retorn: si no ho era no hi ha cap
 * registre per a les seves línies i {@link DesferConsumPlataforma} no fa res. Fer-ho així evita, a més,
 * deixar registres orfes si el magatzem s'ha desmarcat des que es va crear l'albarà.
 */
@Component
public class EliminarAlbaraRetornPlataforma implements EstrategiaEliminarAlbara {

    @Autowired DesferConsumPlataforma desferConsumPlataforma;

    @Override
    public List<TipusAlbara> tipus() {
        return List.of(TipusAlbara.TRASPAS_MAGATZEM);
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

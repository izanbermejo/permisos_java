package ames.comercial.albarans.internal.services.canviardataalbara.strategies;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.consum.SortidaPlataformaRepository;
import ames.comercial.albarans.internal.services.RegistrarRetornPlataforma;
import ames.comercial.albarans.internal.services.canviardataalbara.EstrategiaCanviarDataAlbara;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Propagació per als traspassos que poden ser un retorn de mercaderia d'una plataforma del SII
 * ({@link RegistrarRetornPlataforma}): la traçabilitat de {@code sortides_plataforma} en desa la data
 * com a {@code albconsum_data}, perquè la línia del retorn hi fa de línia de sortida.
 * <p>
 * Només cobreix {@code TRASPAS_MAGATZEM}, que és l'únic tipus que pot ser un retorn (tornar mercaderia
 * d'una plataforma no canvia mai d'empresa). Si l'albarà no era un retorn no hi ha cap registre seu i
 * l'actualització no afecta cap fila.
 */
@Component
public class CanviarDataAlbaraRetornPlataforma implements EstrategiaCanviarDataAlbara {

    @Autowired SortidaPlataformaRepository sortidaPlataformaRepository;

    @Override
    public List<TipusAlbara> tipus() {
        return List.of(TipusAlbara.TRASPAS_MAGATZEM);
    }

    @Override
    public void propagar(Albara albaraAnterior, Albara albaraNou, List<LiniaAlbara> linies) {
        sortidaPlataformaRepository.updateDataConsum(albaraNou.id(), albaraNou.data());
    }

}

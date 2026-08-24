package ames.comercial.albarans.internal.services.canviardataalbara.strategies;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.consum.SortidaPlataformaRepository;
import ames.comercial.albarans.internal.services.canviardataalbara.EstrategiaCanviarDataAlbara;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Propagació per als albarans de consum: la traçabilitat de {@code sortides_plataforma} en desa la data
 * com a {@code albconsum_data}.
 */
@Component
public class CanviarDataAlbaraConsum implements EstrategiaCanviarDataAlbara {

    @Autowired SortidaPlataformaRepository sortidaPlataformaRepository;

    @Override
    public List<TipusAlbara> tipus() {
        return List.of(TipusAlbara.CONSUM);
    }

    @Override
    public void propagar(Albara albaraAnterior, Albara albaraNou, List<LiniaAlbara> linies) {
        sortidaPlataformaRepository.updateDataConsum(albaraNou.id(), albaraNou.data());
    }

}

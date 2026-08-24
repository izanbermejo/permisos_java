package ames.comercial.albarans.internal.services.canviardataalbara;

import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Despatxa l'estratègia de propagació de la nova data segons el tipus d'albarà. Recull totes les
 * estratègies registrades com a beans i, per als tipus sense estratègia específica, retorna
 * {@link EstrategiaCanviarDataAlbara#NO_OP}.
 */
@Component
public class EstrategiesCanviarDataAlbara {

    private final Map<TipusAlbara, EstrategiaCanviarDataAlbara> perTipus;

    public EstrategiesCanviarDataAlbara(List<EstrategiaCanviarDataAlbara> estrategies) {
        this.perTipus = estrategies.stream()
                .flatMap(e -> e.tipus().stream().map(t -> Map.entry(t, e)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public EstrategiaCanviarDataAlbara get(TipusAlbara tipus) {
        return perTipus.getOrDefault(tipus, EstrategiaCanviarDataAlbara.NO_OP);
    }

}

package ames.comercial.albarans.internal.services.eliminaralbara;

import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Despatxa l'estratègia d'eliminació segons el tipus d'albarà. Recull totes les estratègies
 * registrades com a beans i, per als tipus sense estratègia específica, retorna
 * {@link EstrategiaEliminarAlbara#NO_OP}.
 */
@Component
public class EstrategiesEliminarAlbara {

    private final Map<TipusAlbara, EstrategiaEliminarAlbara> perTipus;

    public EstrategiesEliminarAlbara(List<EstrategiaEliminarAlbara> estrategies) {
        this.perTipus = estrategies.stream()
                .flatMap(e -> e.tipus().stream().map(t -> Map.entry(t, e)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public EstrategiaEliminarAlbara get(TipusAlbara tipus) {
        return perTipus.getOrDefault(tipus, EstrategiaEliminarAlbara.NO_OP);
    }

}

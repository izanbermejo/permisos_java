package ames.comercial.inventari.internal.services.desfermoviment;

import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Despatxa l'estratègia de desfer segons el tipus de moviment. Recull totes les estratègies
 * registrades com a beans i, per als tipus sense estratègia específica, retorna
 * {@link EstrategiaDesferMoviment#NO_OP}.
 */
@Component
public class EstrategiesDesferMoviment {

    private final Map<TipusMoviment, EstrategiaDesferMoviment> perTipus;

    public EstrategiesDesferMoviment(List<EstrategiaDesferMoviment> estrategies) {
        this.perTipus = estrategies.stream()
                .collect(Collectors.toMap(EstrategiaDesferMoviment::tipus, Function.identity()));
    }

    public EstrategiaDesferMoviment get(TipusMoviment tipus) {
        return perTipus.getOrDefault(tipus, EstrategiaDesferMoviment.NO_OP);
    }

}

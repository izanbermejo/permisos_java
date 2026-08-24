package ames.comercial.comandes.service.preuquantitat;

import ames.comercial.shared.Preu;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;

import java.util.Map;

import static com.google.common.collect.Range.atLeast;
import static com.google.common.collect.Range.closedOpen;

public class PreuQuantitatNormalitzat implements PreuQuantitat {

    Map<Long, Preu> preus;
    RangeMap<Long, Preu> preusQuantitat = TreeRangeMap.create();

    public PreuQuantitatNormalitzat(Map<Long, Preu> preus) {
        this.preus = preus;
        inicialitzaPreus();
    }

    private void inicialitzaPreus() {
        preusQuantitat.put(closedOpen(0L, 10L), preus.get(1L));
        preusQuantitat.put(closedOpen(10L, 50L), preus.get(2L));
        preusQuantitat.put(closedOpen(50L, 100L), preus.get(3L));
        preusQuantitat.put(closedOpen(100L, 250L), preus.get(4L));
        preusQuantitat.put(closedOpen(250L, 500L), preus.get(5L));
        preusQuantitat.put(closedOpen(500L, 1_000L), preus.get(6L));
        preusQuantitat.put(closedOpen(1_000L, 2_500L), preus.get(7L));
        preusQuantitat.put(closedOpen(2_500L, 5_000L), preus.get(8L));
        preusQuantitat.put(closedOpen(5_000L, 10_000L), preus.get(9L));
        preusQuantitat.put(closedOpen(10_000L, 25_000L), preus.get(10L));
        preusQuantitat.put(closedOpen(25_000L, 50_000L), preus.get(11L));
        preusQuantitat.put(atLeast(50_000L), preus.get(12L));
    }

    @Override
    public Preu aplica(long unitats) {
        return preusQuantitat.get(unitats);
    }

}

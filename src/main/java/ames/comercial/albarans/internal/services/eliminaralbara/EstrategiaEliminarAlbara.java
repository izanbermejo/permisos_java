package ames.comercial.albarans.internal.services.eliminaralbara;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;

import java.util.List;

/**
 * Estratègia amb la lògica específica per tipus d'albarà a l'hora d'eliminar-lo (o eliminar-ne una
 * línia), més enllà del flux comú (esborrar capçalera/línies, desfer moviments i replicar a
 * Advantage). Cada implementació és un bean i declara els {@link TipusAlbara} que cobreix; els tipus
 * sense lògica específica utilitzen {@link #NO_OP}.
 * <p>
 * Els mètodes reben la capçalera i les línies ja carregades perquè quan es criden ja s'han esborrat
 * de la base de dades i no es poden tornar a llegir.
 */
public interface EstrategiaEliminarAlbara {

    List<TipusAlbara> tipus();

    /** Pas específic en eliminar un albarà sencer, amb les línies que tenia. */
    default void desferAlbara(Albara albara, List<LiniaAlbara> linies) {}

    /** Pas específic en eliminar una línia concreta d'un albarà. */
    default void desferLinia(Albara albara, LiniaAlbara linia) {}

    /** Estratègia buida per als tipus d'albarà sense lògica específica. */
    EstrategiaEliminarAlbara NO_OP = List::of;

}

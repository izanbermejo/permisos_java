package ames.comercial.albarans.internal.services.canviardataalbara;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;

import java.util.List;

/**
 * Estratègia amb la lògica específica per tipus d'albarà a l'hora de rectificar-ne la data, més enllà
 * del flux comú (desar la capçalera, actualitzar els moviments d'inventari i replicar a Advantage).
 * Cada implementació és un bean i declara els {@link TipusAlbara} que cobreix; els tipus sense lògica
 * específica utilitzen {@link #NO_OP}.
 */
public interface EstrategiaCanviarDataAlbara {

    List<TipusAlbara> tipus();

    /**
     * Pas específic de propagació de la nova data per a aquest tipus d'albarà.
     *
     * @param albaraAnterior capçalera amb la data antiga, per a qui n'hagi de localitzar registres
     * @param albaraNou      capçalera ja desada amb la data nova
     * @param linies         línies de l'albarà
     */
    default void propagar(Albara albaraAnterior, Albara albaraNou, List<LiniaAlbara> linies) {}

    /** Estratègia buida per als tipus d'albarà sense lògica específica. */
    EstrategiaCanviarDataAlbara NO_OP = List::of;

}

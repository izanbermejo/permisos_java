package ames.comercial.inventari.internal.services.desfermoviment;

import ames.comercial.inventari.internal.domain.moviment.Moviment;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;

/**
 * Estratègia amb la lògica específica per tipus de moviment a l'hora de desfer-lo, més enllà del pas
 * comú (revertir la fitxa d'estoc). Cada implementació és un bean associat a un {@link TipusMoviment};
 * els tipus sense lògica específica utilitzen {@link #NO_OP}.
 */
public interface EstrategiaDesferMoviment {

    TipusMoviment tipus();

    /** Pas específic en desfer un moviment (p. ex. una SORTIDA desfà la línia de comanda associada). */
    default void desfer(Moviment moviment) {}

    /** Estratègia buida per als tipus de moviment sense lògica específica. */
    EstrategiaDesferMoviment NO_OP = () -> null;

}

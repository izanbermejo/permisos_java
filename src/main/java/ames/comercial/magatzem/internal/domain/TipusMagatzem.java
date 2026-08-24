package ames.comercial.magatzem.internal.domain;

/**
 * Tipus de magatzem. Substitueix el camp {@code mag.tipus} de l'Advantage ('P' / 'N').
 * <ul>
 *   <li>{@link #PLATAFORMA}: magatzem de plataforma (client). És l'únic tipus on es fan consums i on,
 *       per tant, es manté el pendent de consumir dels traspassos que hi entren.</li>
 *   <li>{@link #AMES}: magatzem controlat internament per AMES. No pot obrir caixes, per això la
 *       quantitat a servir hauria de ser múltiple de les unitats d'embalatge.</li>
 *   <li>{@link #TRANSIT}: magatzem marcat com a plataforma a l'Advantage però que no ho és de veritat:
 *       la mercaderia només hi està de pas, no s'hi fan consums i no s'hi manté pendent de consumir.
 *       Com l'{@link #AMES}, és controlat internament (no pot obrir caixes).</li>
 * </ul>
 */
public enum TipusMagatzem {
    PLATAFORMA,
    AMES,
    TRANSIT
}

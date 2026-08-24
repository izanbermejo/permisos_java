package ames.comercial.albarans.internal.domain.consum;

import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.shared.KeyArticleClient;

import java.time.LocalDate;

/**
 * Registre de traçabilitat d'una sortida de mercaderia d'un magatzem plataforma marcat per al SII:
 * enllaça la línia que la treu de la plataforma amb la línia de l'albarà de traspàs de la qual s'ha
 * descomptat (FIFO) el pendent de consumir, i la quantitat. Permet desfer la sortida restablint el
 * pendent de consumir a la línia de traspàs corresponent, i és el que llegeix el procés que genera els
 * fitxers del SII.
 * <p>
 * La línia de sortida pot ser de dues menes, i <b>els dos magatzems del registre volen dir coses
 * diferents en cada cas</b>, seguint la convenció que ja feia servir el Delphi:
 * <ul>
 *   <li><b>Consum</b> ({@code AfegirLiniesConsum}): la mercaderia va de la plataforma al client.
 *       {@code magatzemConsum} és la plataforma d'on surt i {@code magatzemTraspas} el magatzem que
 *       havia emès el traspàs d'entrada (mantalb.pas:3635 i 3643).</li>
 *   <li><b>Retorn</b> ({@code RegistrarRetornPlataforma}): la mercaderia torna de la plataforma a un
 *       magatzem d'AMES. {@code magatzemConsum} és el magatzem que la rep i {@code magatzemTraspas} és
 *       la plataforma d'on surt (mantalb.pas:3834 i 3842).</li>
 * </ul>
 */
public record SortidaPlataforma(
        KeyLiniaAlbara albaraConsum,
        String magatzemConsum,
        LocalDate dataConsum,
        KeyArticleClient articleClient,
        long quantitat,
        KeyLiniaAlbara albaraTraspas,
        String magatzemTraspas,
        LocalDate dataTraspas,
        /** Quantitat total de la línia de l'albarà de traspàs d'origen */
        long albtraspasQuantitat
) {}

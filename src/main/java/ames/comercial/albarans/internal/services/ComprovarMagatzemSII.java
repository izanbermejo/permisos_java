package ames.comercial.albarans.internal.services;

import ames.comercial.magatzem.ext.IObtenirMagatzems;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Diu si les sortides de mercaderia d'un magatzem s'han de declarar al SII de la hisenda pública
 * ({@code com_magatzem.magatzem.is_sii}). És l'equivalent de la funció {@code EsMagSII} del Delphi.
 * <p>
 * Només es marquen magatzems de plataforma, i la marca condiciona tot el circuit de consums:
 * <ul>
 *   <li>Les línies dels albarans de traspàs cap a la plataforma només neixen amb quantitat pendent
 *       de consumir si està marcada ({@code CrearAlbaraTraspasPlataforma}, {@code AfegirLiniaTraspas}).</li>
 *   <li>Els consums només descompten aquest pendent i deixen apunt a
 *       {@code albarans.sortides_plataforma} si està marcada ({@link AfegirLiniesConsum}); als altres
 *       magatzems el consum es fa igualment, però sense pendent ni traçabilitat.</li>
 *   <li>Els retorns de mercaderia de la plataforma cap a un magatzem d'AMES fan el mateix
 *       ({@link RegistrarRetornPlataforma}).</li>
 * </ul>
 */
@Service
public class ComprovarMagatzemSII {

    @Autowired IObtenirMagatzems obtenirMagatzems;

    public boolean executar(String magatzem) {
        return obtenirMagatzems.get(magatzem).map(m -> m.isSii()).orElse(false);
    }

}

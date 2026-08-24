package ames.comercial.magatzem.ext;

import ames.comercial.magatzem.internal.domain.Magatzem;

import java.util.List;
import java.util.Optional;

public interface IObtenirMagatzems {
    Optional<Magatzem> get(String codi);
    List<Magatzem> all();

    /**
     * Retorna el magatzem intermig (relleu) per on ha de passar la mercaderia en un traspàs de
     * {@code magatzemInicial} a {@code magatzemFinal}, si n'hi ha. La relació es defineix a
     * {@code com_magatzem.magatzems_intermitjos} per la parella (inicial, final). Buit si no hi ha relleu.
     */
    Optional<Magatzem> obtenirMagatzemIntermig(String magatzemInicial, String magatzemFinal);
}

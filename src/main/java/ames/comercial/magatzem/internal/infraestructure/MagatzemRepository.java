package ames.comercial.magatzem.internal.infraestructure;

import ames.comercial.magatzem.internal.domain.Magatzem;

import java.util.List;
import java.util.Optional;

public interface MagatzemRepository {
    Optional<Magatzem> find(String codi);
    List<Magatzem> findAll();
    void save(Magatzem magatzem);
    void deleteAll();

    /** Codi del magatzem intermig (relleu) per a la parella (inicial, final), si existeix. */
    Optional<String> findCodiIntermig(String magatzemInicial, String magatzemFinal);
}

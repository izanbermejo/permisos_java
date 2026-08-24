package ames.comercial.entrades.internal.infraestructure.comercial;

import ames.comercial.entrades.internal.domain.EntradaComercial;

import java.util.List;
import java.util.Optional;

public interface EntradaComercialRepository {

    void save(EntradaComercial entradaComercial);
    void save(List<EntradaComercial> entradaComercial);

    List<EntradaComercial> pendentsProcessar();
    List<EntradaComercial> obtenirByIdFabrica(String idEntradaFabrica);
    Optional<EntradaComercial> find(String id);

}

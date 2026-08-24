package ames.comercial.entrades.internal.infraestructure.magatzem;

import ames.comercial.entrades.internal.domain.EntradaMagatzem;

import java.util.List;

public interface EntradaMagatzemRepository {

    void save(EntradaMagatzem entradaMagatzem);
    void save(List<EntradaMagatzem> entradesMagatzem);

    List<EntradaMagatzem> pendentsProcessar();

}

package ames.comercial.entrades.internal.infraestructure.magatzem;

import ames.comercial.entrades.internal.domain.ErrorEntradaMagatzem;

import java.util.List;

public interface ErrorEntradaMagatzemRepository {
    void guardarError(String idEntrada, String magatzem);
    void marcarEnviat(String idEntrada);
    List<ErrorEntradaMagatzem> listarPendents();
}

package ames.comercial.entrades.internal.infraestructure.comercial;

import ames.comercial.entrades.internal.domain.ErrorEntradaComercial;

import java.util.List;

public interface ErrorEntradaComercialRepository {
    void guardarError(String idEntrada);
    void marcarEnviat(String idEntrada);
    List<ErrorEntradaComercial> listarPendents();
}

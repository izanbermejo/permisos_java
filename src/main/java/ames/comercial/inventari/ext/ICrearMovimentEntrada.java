package ames.comercial.inventari.ext;

import ames.comercial.inventari.internal.domain.service.FactoryMovimentEntrada.CrearMovimentEntradaRequest;

public interface ICrearMovimentEntrada {

    void executar(CrearMovimentEntradaRequest request);

}

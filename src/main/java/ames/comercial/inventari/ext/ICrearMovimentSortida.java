package ames.comercial.inventari.ext;

import ames.comercial.inventari.internal.domain.service.FactoryMovimentSortida.CrearMovimentSortidaRequest;

public interface ICrearMovimentSortida {

    void executar(CrearMovimentSortidaRequest request);

}

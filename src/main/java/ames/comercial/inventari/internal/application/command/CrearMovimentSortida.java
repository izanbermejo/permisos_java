package ames.comercial.inventari.internal.application.command;

import ames.comercial.inventari.ext.ICrearMovimentSortida;
import ames.comercial.inventari.internal.domain.service.FactoryMovimentSortida;
import ames.comercial.inventari.internal.domain.service.FactoryMovimentSortida.CrearMovimentSortidaRequest;
import ames.comercial.inventari.internal.infraestructure.moviment.MovimentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CrearMovimentSortida implements ICrearMovimentSortida {

    @Autowired MovimentRepository movimentRepository;
    @Autowired ActualitzarFitxa actualitzarFitxa;

    @Override
    public void executar(CrearMovimentSortidaRequest request) {
        // Creació del moviment de sortida
        var moviment = new FactoryMovimentSortida().executar(request);
        // Es guarda el moviment a l'històric
        movimentRepository.save(moviment);
        // Actualització de la fitxa d'inventari
        actualitzarFitxa.executar(moviment);
    }

}

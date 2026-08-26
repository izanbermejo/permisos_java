package ames.permisos.aplicacions.internal.application.command;

import ames.permisos.aplicacions.AplicacionsException;
import ames.permisos.aplicacions.internal.infraestructure.AplicacioRepository;
import ames.permisos.aplicacions.internal.infraestructure.AplicacioRepositorySql;
import ames.permisos.server.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarAplicacio {

    @Autowired AplicacioRepository aplicacioRepository;

    public void executar(String nomAplicacio) {
        var assignada = aplicacioRepository.estaAssignada(nomAplicacio);

        if (assignada) throw new AplicacionsException.AplicacioAssignada();
        eliminar(nomAplicacio);
    }

    public void eliminar(String nomAplicacio) {
        BeanUtils.getBean(AplicacioRepositorySql.class).delete(nomAplicacio);
    }
}

package ames.permisos.aplicacions.internal.application.command;

import ames.permisos.aplicacions.AplicacionsException;
import ames.permisos.aplicacions.internal.domain.Aplicacio;
import ames.permisos.aplicacions.internal.infraestructure.AplicacioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrearAplicacio {

    @Autowired AplicacioRepository aplicacioRepo;

    public void executar(Aplicacio aplicacio) {
        if (aplicacioRepo.find(aplicacio.nomAplicacio()).isPresent()) throw new AplicacionsException.NomAplicacioJaExisteix();
        aplicacioRepo.save(aplicacio.nomAplicacio(), aplicacio.descripcio());
    }
}
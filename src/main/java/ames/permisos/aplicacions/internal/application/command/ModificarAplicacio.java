package ames.permisos.aplicacions.internal.application.command;

import ames.permisos.aplicacions.internal.infraestructure.AplicacioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModificarAplicacio {

    @Autowired AplicacioRepository aplicacioRepo;

    public void executar(String nomAplicacio, String descripcio) {
        aplicacioRepo.save(nomAplicacio, descripcio);
    }
}
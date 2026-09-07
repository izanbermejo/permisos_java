package ames.permisos.parametres.internal.application.command;

import ames.permisos.parametres.internal.infraestructure.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModificarParametre {

    @Autowired
    ParametreRepository parametreRepo;

    public void executar(String nomAplicacio, String nomParametre, String descripcio) {
        parametreRepo.save(nomAplicacio, nomParametre, descripcio);
    }
}
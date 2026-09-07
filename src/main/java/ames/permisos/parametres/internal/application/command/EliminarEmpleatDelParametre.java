package ames.permisos.parametres.internal.application.command;

import ames.permisos.parametres.internal.infraestructure.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarEmpleatDelParametre {

    @Autowired
    ParametreRepository parametreRepo;

    public void executar(String nomAplicacio, String nomParametre, int idEmpleat) {
        parametreRepo.deleteEmpleat(nomAplicacio, nomParametre, idEmpleat);
    }
}

package ames.permisos.parametres.internal.application.command;

import ames.permisos.parametres.ParametresException;
import ames.permisos.parametres.internal.infraestructure.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignarEmpleatParametre {

    @Autowired
    ParametreRepository parametreRepo;

    public void executar(String nomAplicacio, String nomParametre, String valor, int idEmpleat) {
        if (parametreRepo.findAssignacioEmpleat(nomAplicacio, nomParametre, idEmpleat).isPresent()) throw new ParametresException.EmpleatParametreJaExisteix();
        parametreRepo.saveAssignacioEmpleat(nomAplicacio, nomParametre, valor, idEmpleat);
    }
}
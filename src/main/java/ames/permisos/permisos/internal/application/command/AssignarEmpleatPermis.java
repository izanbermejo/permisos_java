package ames.permisos.permisos.internal.application.command;

import ames.permisos.permisos.PermisosException;
import ames.permisos.permisos.internal.infraestructure.PermisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignarEmpleatPermis {

    @Autowired
    PermisRepository permisRepo;

    public void executar(String nomAplicacio, String nomModul, String nomPermis, int idEmpleat) {
        if (permisRepo.findAssignacioEmpleat(nomAplicacio, nomModul, nomPermis, idEmpleat).isPresent()) throw new PermisosException.EmpleatPermisJaExisteix();
        permisRepo.saveAssignacioEmpleat(nomAplicacio, nomModul, nomPermis, idEmpleat);
    }
}
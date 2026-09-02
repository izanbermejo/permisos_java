package ames.permisos.moduls.internal.application.command;

import ames.permisos.aplicacions.AplicacionsException;
import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignarEmpleat {

    @Autowired ModulRepository modulRepo;

    public void executar(String nomAplicacio, String nomModul, int idEmpleat) {
        if (modulRepo.findAssignacioEmpleat(nomAplicacio, nomModul, idEmpleat).isPresent()) throw new AplicacionsException.EmpleatModulJaExisteix();
        modulRepo.saveAssignacioEmpleat(nomAplicacio, nomModul, idEmpleat);
    }
}
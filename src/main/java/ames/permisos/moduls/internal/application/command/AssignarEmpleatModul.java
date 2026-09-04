package ames.permisos.moduls.internal.application.command;

import ames.permisos.moduls.ModulsException;
import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignarEmpleatModul {

    @Autowired ModulRepository modulRepo;

    public void executar(String nomAplicacio, String nomModul, int idEmpleat) {
        if (modulRepo.findAssignacioEmpleat(nomAplicacio, nomModul, idEmpleat).isPresent()) throw new ModulsException.EmpleatModulJaExisteix();
        modulRepo.saveAssignacioEmpleat(nomAplicacio, nomModul, idEmpleat);
    }
}
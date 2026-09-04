package ames.permisos.moduls.internal.application.command;

import ames.permisos.moduls.ModulsException;
import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import ames.permisos.organigrama.internal.domain.Funcio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignarFuncioModul {

    @Autowired ModulRepository modulRepo;

    public void executar(String nomAplicacio, String nomModul, Funcio funcio) {
        if (modulRepo.findAssignacioFuncio(nomAplicacio, nomModul, funcio).isPresent()) throw new ModulsException.FuncioModulJaExisteix();
        modulRepo.saveAssignacioFuncio(nomAplicacio, nomModul, funcio);
    }
}
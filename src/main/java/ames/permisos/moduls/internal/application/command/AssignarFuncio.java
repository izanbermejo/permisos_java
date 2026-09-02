package ames.permisos.moduls.internal.application.command;

import ames.permisos.aplicacions.AplicacionsException;
import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import ames.permisos.organigrama.internal.domain.Funcio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignarFuncio {

    @Autowired ModulRepository modulRepo;

    public void executar(String nomAplicacio, String nomModul, Funcio funcio) {
        if (modulRepo.findAssignacioFuncio(nomAplicacio, nomModul, funcio).isPresent()) throw new AplicacionsException.FuncioModulJaExisteix();
        modulRepo.saveAssignacioFuncio(nomAplicacio, nomModul, funcio);
    }
}
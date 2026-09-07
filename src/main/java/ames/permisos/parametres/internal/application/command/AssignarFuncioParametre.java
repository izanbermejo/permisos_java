package ames.permisos.parametres.internal.application.command;

import ames.permisos.parametres.ParametresException;
import ames.permisos.parametres.internal.domain.FuncioParametre;
import ames.permisos.parametres.internal.infraestructure.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignarFuncioParametre {

    @Autowired
    ParametreRepository parametreRepo;

    public void executar(String nomAplicacio, String nomParametre, FuncioParametre funcioParametre) {
        if (parametreRepo.findAssignacioFuncio(nomAplicacio, nomParametre, funcioParametre).isPresent()) throw new ParametresException.FuncioParametreJaExisteix();
        parametreRepo.saveAssignacioFuncio(nomAplicacio, nomParametre, funcioParametre);
    }
}
package ames.permisos.permisos.internal.application.command;

import ames.permisos.organigrama.internal.domain.Funcio;
import ames.permisos.permisos.PermisosException;
import ames.permisos.permisos.internal.infraestructure.PermisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignarFuncioPermis {

    @Autowired
    PermisRepository permisRepo;

    public void executar(String nomAplicacio, String nomModul, String nomPermis, Funcio funcio) {
        if (permisRepo.findAssignacioFuncio(nomAplicacio, nomModul, nomPermis, funcio).isPresent()) throw new PermisosException.FuncioPermisJaExisteix();
        permisRepo.saveAssignacioFuncio(nomAplicacio, nomModul, nomPermis, funcio);
    }
}
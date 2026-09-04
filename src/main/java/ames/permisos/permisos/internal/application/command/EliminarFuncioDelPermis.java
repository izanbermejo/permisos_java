package ames.permisos.permisos.internal.application.command;

import ames.permisos.organigrama.internal.domain.Funcio;
import ames.permisos.permisos.internal.infraestructure.PermisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarFuncioDelPermis {

    @Autowired
    PermisRepository permisRepo;

    public void executar(String nomAplicacio, String nomModul, String nomPermis, Funcio funcio) {
        permisRepo.deleteFuncio(nomAplicacio, nomModul, nomPermis, funcio);
    }
}

package ames.permisos.permisos.internal.application.command;

import ames.permisos.permisos.internal.domain.Permis;
import ames.permisos.permisos.internal.infraestructure.PermisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModificarPermis {

    @Autowired
    PermisRepository permisRepo;

    public void executar(Permis permis) {
        permisRepo.save(permis);
    }
}
package ames.permisos.permisos.internal.application.command;

import ames.permisos.permisos.PermisosException;
import ames.permisos.permisos.internal.domain.Permis;
import ames.permisos.permisos.internal.infraestructure.PermisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrearPermis {

    @Autowired
    PermisRepository permisRepo;

    public void executar(Permis permis) {
        if (permisRepo.find(permis.nomAplicacio(), permis.nomModul(), permis.nomPermis()).isPresent()) throw new PermisosException.NomPermisJaExisteix();
        permisRepo.save(permis);
    }
}
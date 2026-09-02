package ames.permisos.permisos.internal.application.query;

import ames.permisos.permisos.internal.domain.Permis;
import ames.permisos.permisos.internal.infraestructure.PermisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirPermisosByAplicacioModul {

    @Autowired PermisRepository permisRepo;

    public List<Permis> executar(String nomAplicacio, String nomModul) {
        return permisRepo.obtenirPermisosByAplicacioModul(nomAplicacio, nomModul);
    }
}
package ames.permisos.permisos.internal.application.query;

import ames.permisos.permisos.internal.domain.Permis;
import ames.permisos.permisos.internal.infraestructure.PermisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirPermisByNom {

    @Autowired
    PermisRepository permisRepo;

    public Optional<Permis> executar(String nomAplicacio, String nomModul, String nomPermis) {
        return permisRepo.find(nomAplicacio, nomModul, nomPermis);
    }
}
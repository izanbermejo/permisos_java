package ames.permisos.permisos.internal.application.query;

import ames.permisos.permisos.internal.domain.Permis;
import ames.permisos.permisos.internal.infraestructure.PermisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ObtenirPermisosByModuls {

    @Autowired PermisRepository permisRepo;

    public List<Permis> executar(String nomAplicacio, List<String> request) {
        List<Permis> permisos = new ArrayList<>();

        request.forEach(modul ->
            permisos.addAll(
                permisRepo.obtenirPermisosByAplicacioModul(nomAplicacio, modul)
            )
        );
        return permisos;
    }
}

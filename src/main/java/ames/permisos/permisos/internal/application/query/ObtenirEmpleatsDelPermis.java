package ames.permisos.permisos.internal.application.query;

import ames.permisos.organigrama.internal.domain.Empleat;
import ames.permisos.permisos.internal.infraestructure.PermisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirEmpleatsDelPermis {

    @Autowired
    PermisRepository permisRepo;

    public List<Empleat> executar(String nomAplicacio, String nomModul, String nomPermis) {
        return permisRepo.listEmpleatDelPermis(nomAplicacio, nomModul, nomPermis);
    }
}
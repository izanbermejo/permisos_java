package ames.permisos.parametres.internal.application.query;

import ames.permisos.parametres.internal.domain.EmpleatParametre;
import ames.permisos.parametres.internal.infraestructure.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirTotsElsEmpleatsDelParametre {

    @Autowired
    ParametreRepository parametreRepo;

    public List<EmpleatParametre> executar(String nomAplicacio, String nomModul) {
        return parametreRepo.listAllEmpleatDelParametre(nomAplicacio, nomModul);
    }
}
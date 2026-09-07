package ames.permisos.parametres.internal.application.query;

import ames.permisos.parametres.internal.domain.Parametre;
import ames.permisos.parametres.internal.infraestructure.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirParametreByNom {

    @Autowired
    ParametreRepository parametreRepo;

    public Optional<Parametre> executar(String nomAplicacio, String nomParametre) {
        return parametreRepo.find(nomAplicacio, nomParametre);
    }
}
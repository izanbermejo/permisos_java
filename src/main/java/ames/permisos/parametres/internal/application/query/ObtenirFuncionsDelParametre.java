package ames.permisos.parametres.internal.application.query;

import ames.permisos.parametres.internal.domain.FuncioParametre;
import ames.permisos.parametres.internal.infraestructure.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirFuncionsDelParametre {

    @Autowired
    ParametreRepository parametreRepo;

    public List<FuncioParametre> executar(String nomAplicacio, String nomParametre) {
        return parametreRepo.listFuncioDelParametre(nomAplicacio, nomParametre);
    }
}
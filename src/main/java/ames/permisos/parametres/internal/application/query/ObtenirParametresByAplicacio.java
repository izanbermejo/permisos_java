package ames.permisos.parametres.internal.application.query;

import ames.permisos.parametres.internal.domain.Parametre;
import ames.permisos.parametres.internal.infraestructure.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ObtenirParametresByAplicacio {

    @Autowired
    ParametreRepository parametreRepo;

    public List<Parametre> executar(List<String> request) {
        List<Parametre> parametres = new ArrayList<>();

        request.forEach(aplicacio ->
                parametres.addAll(
                        parametreRepo.obtenirParametresByAplicacio(aplicacio)
                )
        );
        return parametres;
    }

    public List<Parametre> executar(String nomAplicacio) {
        return parametreRepo.obtenirParametresByAplicacio(nomAplicacio);
    }
}
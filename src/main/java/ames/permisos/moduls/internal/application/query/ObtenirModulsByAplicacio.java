package ames.permisos.moduls.internal.application.query;

import ames.permisos.moduls.internal.domain.Modul;
import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ObtenirModulsByAplicacio {

    @Autowired
    ModulRepository modulRepo;

    public List<Modul> executar(List<String> request) {
        List<Modul> moduls = new ArrayList<>();

        request.forEach(aplicacio ->
            moduls.addAll(
                modulRepo.obtenirModulsByAplicacio(aplicacio)
            )
        );
        return moduls;
    }

    public List<Modul> executar(String nomAplicacio) {
        return modulRepo.obtenirModulsByAplicacio(nomAplicacio);
    }
}

package ames.permisos.moduls.internal.application.query;

import ames.permisos.moduls.internal.domain.Modul;
import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirModulByNom {

    @Autowired ModulRepository modulRepo;

    public Optional<Modul> executar(String nomAplicacio, String nomModul) {
        return modulRepo.find(nomAplicacio, nomModul);
    }
}
package ames.permisos.moduls.internal.application.query;

import ames.permisos.moduls.internal.domain.Modul;
import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirModuls {

    @Autowired ModulRepository modulRepo;

    public List<Modul> executar() {
        return modulRepo.list();
    }
}
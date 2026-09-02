package ames.permisos.moduls.internal.application.query;

import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import ames.permisos.organigrama.internal.domain.Empleat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirEmpleatsDelModul {

    @Autowired ModulRepository modulRepo;

    public List<Empleat> executar(String nomAplicacio, String nomModul) {
        return modulRepo.listEmpleatDelModul(nomAplicacio, nomModul);
    }
}
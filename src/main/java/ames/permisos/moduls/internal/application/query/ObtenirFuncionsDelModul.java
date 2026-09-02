package ames.permisos.moduls.internal.application.query;

import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import ames.permisos.organigrama.internal.domain.Funcio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirFuncionsDelModul {

    @Autowired ModulRepository modulRepo;

    public List<Funcio> executar(String nomAplicacio, String nomModul) {
        return modulRepo.listFuncioDelModul(nomAplicacio, nomModul);
    }
}
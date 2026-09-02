package ames.permisos.moduls.internal.application.command;

import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import ames.permisos.organigrama.internal.domain.Funcio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarFuncioDelModul {

    @Autowired ModulRepository modulRepo;

    public void executar(String nomAplicacio, String nomModul, Funcio funcio) {
        modulRepo.deleteFuncio(nomAplicacio, nomModul, funcio);
    }
}

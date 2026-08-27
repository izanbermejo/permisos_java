package ames.permisos.moduls.internal.application.command;

import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ModificarModul {

    @Autowired ModulRepository modulRepo;

    public void executar(String nomAplicacio, String nomModul, String descripcio) {
        modulRepo.save(nomAplicacio, nomModul, descripcio);
    }
}
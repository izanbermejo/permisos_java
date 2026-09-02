package ames.permisos.moduls.internal.application.command;

import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarEmpleatDelModul {

    @Autowired ModulRepository modulRepo;

    public void executar(String nomAplicacio, String nomModul, int idEmpleat) {
        modulRepo.deleteEmpleat(nomAplicacio, nomModul, idEmpleat);
    }
}

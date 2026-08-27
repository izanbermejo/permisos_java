package ames.permisos.moduls.internal.application.command;

import ames.permisos.moduls.ModulsException;
import ames.permisos.moduls.internal.domain.Modul;
import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrearModul {

    @Autowired ModulRepository modulRepo;

    public void executar(Modul modul) {
        if (modulRepo.find(modul.nomAplicacio(), modul.nomModul()).isPresent()) throw new ModulsException.NomModulJaExisteix();
        modulRepo.save(modul.nomAplicacio(), modul.nomModul(), modul.descripcio());
    }
}
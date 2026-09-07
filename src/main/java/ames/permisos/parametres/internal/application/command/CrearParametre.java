package ames.permisos.parametres.internal.application.command;

import ames.permisos.parametres.ParametresException;
import ames.permisos.parametres.internal.domain.Parametre;
import ames.permisos.parametres.internal.infraestructure.ParametreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrearParametre {

    @Autowired
    ParametreRepository parametreRepo;

    public void executar(Parametre parametre) {
        if (parametreRepo.find(parametre.nomAplicacio(), parametre.nomParametre()).isPresent()) throw new ParametresException.NomParametreJaExisteix();
        parametreRepo.save(parametre.nomAplicacio(), parametre.nomParametre(), parametre.descripcio());
    }
}
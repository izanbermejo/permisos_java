package ames.permisos.parametres.internal.application.command;

import ames.permisos.parametres.internal.application.query.ComprovacioAssignacionsParametre;
import ames.permisos.parametres.internal.infraestructure.ParametreRepository;
import ames.permisos.shared.ResultatEliminacio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarParametre {

    @Autowired
    ParametreRepository parametreRepo;
    @Autowired
    ComprovacioAssignacionsParametre comprovacioAssignacionsParametre;

    public ResultatEliminacio executar(String nomAplicacio, String nomParametre, boolean confirmar) {

        if (!confirmar && comprovacioAssignacionsParametre.executar(nomAplicacio, nomParametre).requereixConfirmacio()) {
            return new ResultatEliminacio(true);
        }

        eliminar(nomAplicacio, nomParametre);

        return new ResultatEliminacio(false);
    }

    public void eliminar(String nomAplicacio, String nomParametre) {
        parametreRepo.delete(nomAplicacio, nomParametre);
    }
}

package ames.permisos.permisos.internal.application.command;

import ames.permisos.permisos.internal.application.query.ComprovacioAssignacionsPermis;
import ames.permisos.permisos.internal.infraestructure.PermisRepository;
import ames.permisos.shared.ResultatEliminacio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarPermis {

    @Autowired
    PermisRepository permisRepo;
    @Autowired
    ComprovacioAssignacionsPermis comprovacioAssignacionsPermis;

    public ResultatEliminacio executar(String nomAplicacio, String nomModul, String nomPermis, boolean confirmar) {

        if (!confirmar && comprovacioAssignacionsPermis.executar(nomAplicacio, nomModul, nomPermis).requereixConfirmacio()) {
            return new ResultatEliminacio(true);
        }

        eliminar(nomAplicacio, nomModul, nomPermis);

        return new ResultatEliminacio(false);
    }

    public void eliminar(String nomAplicacio, String nomModul, String nomPermis) {
        permisRepo.delete(nomAplicacio, nomModul, nomPermis);
    }
}

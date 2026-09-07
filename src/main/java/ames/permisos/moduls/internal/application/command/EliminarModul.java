package ames.permisos.moduls.internal.application.command;

import ames.permisos.moduls.internal.application.query.ComprovacioAssignacionsModul;
import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import ames.permisos.shared.ResultatEliminacio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarModul {

    @Autowired
    ModulRepository modulRepo;
    @Autowired
    ComprovacioAssignacionsModul comprovacioAssignacionsModul;

    public ResultatEliminacio executar(String nomAplicacio, String nomModul, boolean confirmar) {

        if (!confirmar && comprovacioAssignacionsModul.executar(nomAplicacio, nomModul).requereixConfirmacio()) {
            return new ResultatEliminacio(true);
        }

        eliminar(nomAplicacio, nomModul);

        return new ResultatEliminacio(false);
    }

    public void eliminar(String nomAplicacio, String nomModul) {
        modulRepo.delete(nomAplicacio, nomModul);
    }
}

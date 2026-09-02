package ames.permisos.moduls.internal.application.command;

import ames.permisos.moduls.ModulsException;
import ames.permisos.moduls.internal.application.query.ObtenirEmpleatsDelModul;
import ames.permisos.moduls.internal.application.query.ObtenirFuncionsDelModul;
import ames.permisos.moduls.internal.infraestructure.ModulRepository;
import ames.permisos.permisos.internal.application.query.ObtenirPermisosByAplicacioModul;
import ames.permisos.server.BeanUtils;
import ames.permisos.shared.ResultatEliminacio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarModul {

    @Autowired ModulRepository modulRepo;

    public ResultatEliminacio executar(String nomAplicacio, String nomModul, boolean confirmar) {
        var permisos = BeanUtils.getBean(ObtenirPermisosByAplicacioModul.class).executar(nomAplicacio, nomModul);
        if (!permisos.isEmpty()) throw new ModulsException.ModulAssignat();

        if(!confirmar) {
            var funcions = BeanUtils.getBean(ObtenirFuncionsDelModul.class).executar(nomAplicacio, nomModul);
            var empleats = BeanUtils.getBean(ObtenirEmpleatsDelModul.class).executar(nomAplicacio, nomModul);
            if (!funcions.isEmpty() || !empleats.isEmpty()) return new ResultatEliminacio(true);
        }

        eliminar(nomAplicacio, nomModul);

        return new ResultatEliminacio(false);
    }

    public void eliminar(String nomAplicacio, String nomModul) {
        modulRepo.delete(nomAplicacio, nomModul);
    }
}

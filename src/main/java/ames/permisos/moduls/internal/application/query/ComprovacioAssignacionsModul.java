package ames.permisos.moduls.internal.application.query;

import ames.permisos.moduls.ModulsException;
import ames.permisos.permisos.internal.application.query.ObtenirPermisosByAplicacioModul;
import ames.permisos.shared.ResultatEliminacio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComprovacioAssignacionsModul {

    @Autowired
    ObtenirPermisosByAplicacioModul obtenirPermisosByAplicacioModul;
    @Autowired
    ObtenirFuncionsDelModul obtenirFuncionsDelModul;
    @Autowired
    ObtenirEmpleatsDelModul obtenirEmpleatsDelModul;

    public ResultatEliminacio executar(String nomAplicacio, String nomModul) {
        var permisos = obtenirPermisosByAplicacioModul.executar(nomAplicacio, nomModul);
        if (!permisos.isEmpty()) throw new ModulsException.ModulAssignat();

        var funcions = obtenirFuncionsDelModul.executar(nomAplicacio, nomModul);
        var empleats = obtenirEmpleatsDelModul.executar(nomAplicacio, nomModul);
        if (!funcions.isEmpty() || !empleats.isEmpty()) {
            return new ResultatEliminacio(true);
        } else {
            return new ResultatEliminacio(false);
        }
    }
}

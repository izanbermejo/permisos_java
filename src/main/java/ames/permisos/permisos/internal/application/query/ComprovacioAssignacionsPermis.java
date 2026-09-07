package ames.permisos.permisos.internal.application.query;

import ames.permisos.shared.ResultatEliminacio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComprovacioAssignacionsPermis {

    @Autowired
    ObtenirFuncionsDelPermis obtenirFuncionsDelPermis;
    @Autowired
    ObtenirEmpleatsDelPermis obtenirEmpleatsDelPermis;

    public ResultatEliminacio executar(String nomAplicacio, String nomModul, String nomPermis) {
        var funcions = obtenirFuncionsDelPermis.executar(nomAplicacio, nomModul, nomPermis);
        var empleats = obtenirEmpleatsDelPermis.executar(nomAplicacio, nomModul, nomPermis);
        if (!funcions.isEmpty() || !empleats.isEmpty()) {
            return new ResultatEliminacio(true);
        } else {
            return new ResultatEliminacio(false);
        }
    }
}

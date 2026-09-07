package ames.permisos.parametres.internal.application.query;

import ames.permisos.shared.ResultatEliminacio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ComprovacioAssignacionsParametre {

    @Autowired
    ObtenirFuncionsDelParametre obtenirFuncionsDelParametre;
    @Autowired
    ObtenirEmpleatsDelParametre obtenirEmpleatsDelParametre;

    public ResultatEliminacio executar(String nomAplicacio, String nomParametre) {
        var funcions = obtenirFuncionsDelParametre.executar(nomAplicacio, nomParametre);
        var empleats = obtenirEmpleatsDelParametre.executar(nomAplicacio, nomParametre);
        if (!funcions.isEmpty() || !empleats.isEmpty()) {
            return new ResultatEliminacio(true);
        } else {
            return new ResultatEliminacio(false);
        }
    }
}

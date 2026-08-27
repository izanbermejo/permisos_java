package ames.permisos.moduls.internal.application.command;

import ames.permisos.moduls.ModulsException;
import ames.permisos.moduls.internal.infraestructure.ModulRepositorySql;
import ames.permisos.permisos.internal.application.query.ObtenirPermisosByAplicacioModul;
import ames.permisos.server.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class EliminarModul {

    public void executar(String nomAplicacio, String nomModul) {
        var permisos = BeanUtils.getBean(ObtenirPermisosByAplicacioModul.class).executar(nomAplicacio, nomModul);

        if (!permisos.isEmpty()) throw new ModulsException.ModulAssignat();
        eliminar(nomAplicacio, nomModul);
    }

    public void eliminar(String nomAplicacio, String nomModul) {
        BeanUtils.getBean(ModulRepositorySql.class).delete(nomAplicacio, nomModul);
    }
}

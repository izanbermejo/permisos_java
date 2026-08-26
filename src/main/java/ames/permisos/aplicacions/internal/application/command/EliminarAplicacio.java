package ames.permisos.aplicacions.internal.application.command;

import ames.permisos.aplicacions.AplicacionsException;
import ames.permisos.aplicacions.internal.infraestructure.AplicacioRepositorySql;
import ames.permisos.moduls.internal.application.query.ObtenirModulsByAplicacio;
import ames.permisos.parametres.internal.application.query.ObtenirParametresByAplicacio;
import ames.permisos.server.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class EliminarAplicacio {

    public void executar(String nomAplicacio) {
        var moduls = BeanUtils.getBean(ObtenirModulsByAplicacio.class).executar(nomAplicacio);
        var parametres = BeanUtils.getBean(ObtenirParametresByAplicacio.class).executar(nomAplicacio);

        if (!moduls.isEmpty() || !parametres.isEmpty()) throw new AplicacionsException.AplicacioAssignada();
        eliminar(nomAplicacio);
    }

    public void eliminar(String nomAplicacio) {
        BeanUtils.getBean(AplicacioRepositorySql.class).delete(nomAplicacio);
    }
}

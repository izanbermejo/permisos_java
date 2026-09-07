package ames.permisos.parametres.internal.application.command;

import ames.permisos.parametres.internal.application.query.ObtenirEmpleatsDelParametre;
import ames.permisos.parametres.internal.application.query.ObtenirFuncionsDelParametre;
import ames.permisos.parametres.internal.infraestructure.ParametreRepository;
import ames.permisos.server.BeanUtils;
import ames.permisos.shared.ResultatEliminacio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarParametre {

    @Autowired
    ParametreRepository parametreRepo;

    public ResultatEliminacio executar(String nomAplicacio, String nomParametre, boolean confirmar) {

        if(!confirmar) {
            var funcions = BeanUtils.getBean(ObtenirFuncionsDelParametre.class).executar(nomAplicacio, nomParametre);
            var empleats = BeanUtils.getBean(ObtenirEmpleatsDelParametre.class).executar(nomAplicacio, nomParametre);
            if (!funcions.isEmpty() || !empleats.isEmpty()) return new ResultatEliminacio(true);
        }

        eliminar(nomAplicacio, nomParametre);

        return new ResultatEliminacio(false);
    }

    public void eliminar(String nomAplicacio, String nomParametre) {
        parametreRepo.delete(nomAplicacio, nomParametre);
    }
}

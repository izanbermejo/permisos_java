package ames.permisos.permisos.internal.application.command;

import ames.permisos.permisos.internal.application.query.ObtenirEmpleatsDelPermis;
import ames.permisos.permisos.internal.application.query.ObtenirFuncionsDelPermis;
import ames.permisos.permisos.internal.infraestructure.PermisRepository;
import ames.permisos.server.BeanUtils;
import ames.permisos.shared.ResultatEliminacio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EliminarPermis {

    @Autowired
    PermisRepository permisRepo;

    public ResultatEliminacio executar(String nomAplicacio, String nomModul, String nomPermis, boolean confirmar) {

        if(!confirmar) {
            var funcions = BeanUtils.getBean(ObtenirFuncionsDelPermis.class).executar(nomAplicacio, nomModul, nomPermis);
            var empleats = BeanUtils.getBean(ObtenirEmpleatsDelPermis.class).executar(nomAplicacio, nomModul, nomPermis);
            if (!funcions.isEmpty() || !empleats.isEmpty()) return new ResultatEliminacio(true);
        }

        eliminar(nomAplicacio, nomModul, nomPermis);

        return new ResultatEliminacio(false);
    }

    public void eliminar(String nomAplicacio, String nomModul, String nomPermis) {
        permisRepo.delete(nomAplicacio, nomModul, nomPermis);
    }
}

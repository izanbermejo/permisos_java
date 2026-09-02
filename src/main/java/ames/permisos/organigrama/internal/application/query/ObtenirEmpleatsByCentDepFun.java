package ames.permisos.organigrama.internal.application.query;

import ames.permisos.organigrama.internal.domain.Empleat;
import ames.permisos.organigrama.internal.infraestructure.EmpleatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirEmpleatsByCentDepFun {

    @Autowired EmpleatRepository empleatRepo;

    public List<Empleat> executar(Integer idCentre, Integer idDepartament, Integer idFuncio) {
        return empleatRepo.obtenirEmpleatsByCentDepFun(idCentre, idDepartament, idFuncio);
    }
}
package ames.permisos.organigrama.internal.application.query;

import ames.permisos.organigrama.internal.domain.Funcio;
import ames.permisos.organigrama.internal.infraestructure.FuncioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObtenirFuncionsByEmpleat {

    @Autowired FuncioRepository funcioRepo;

    public List<Funcio> executar(int idEmpleat) {
        return funcioRepo.obtenirFuncionsByEmpleat(idEmpleat);
    }
}
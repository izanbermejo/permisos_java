package ames.permisos.organigrama.internal.infraestructure;

import ames.permisos.organigrama.internal.domain.Funcio;

import java.util.List;

public interface FuncioRepository {
    List<Funcio> obtenirFuncionsByEmpleat(int idEmpleat);
}

package ames.permisos.organigrama.internal.infraestructure;

import ames.permisos.organigrama.internal.domain.Empleat;

import java.util.List;

public interface EmpleatRepository {
    List<Empleat> obtenirEmpleatsByCentDepFun(Integer idCentre, Integer idDepartament, Integer idFuncio);
}

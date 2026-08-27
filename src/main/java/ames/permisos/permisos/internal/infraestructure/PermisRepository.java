package ames.permisos.permisos.internal.infraestructure;

import ames.permisos.permisos.internal.domain.Permis;

import java.util.List;

public interface PermisRepository {

    List<Permis> obtenirPermisosByAplicacioModul(String nomAplicacio, String nomModul);

}

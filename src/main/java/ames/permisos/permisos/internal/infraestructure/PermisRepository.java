package ames.permisos.permisos.internal.infraestructure;

import ames.permisos.organigrama.internal.domain.Empleat;
import ames.permisos.organigrama.internal.domain.Funcio;
import ames.permisos.permisos.internal.domain.Permis;

import java.util.List;
import java.util.Optional;

public interface PermisRepository {

    List<Permis> obtenirPermisosByAplicacioModul(String nomAplicacio, String nomModul);
    Optional<Permis> find(String nomAplicacio, String nomModul, String nomPermis);
    void save(Permis permis);
    void delete(String nomAplicacio, String nomModul, String nomPermis);
    List<Funcio> listFuncioDelPermis(String nomAplicacio, String nomModul, String nomPermis);
    void deleteFuncio(String nomAplicacio, String nomModul, String nomPermis, Funcio funcio);
    Optional<Funcio> findAssignacioFuncio(String nomAplicacio, String nomModul, String nomPermis, Funcio funcio);
    void saveAssignacioFuncio(String nomAplicacio, String nomModul, String nomPermis, Funcio funcio);
    List<Empleat> listEmpleatDelPermis(String nomAplicacio, String nomModul, String nomPermis);
    Optional<Empleat> findAssignacioEmpleat(String nomAplicacio, String nomModul, String nomPermis, int idEmpleat);
    void saveAssignacioEmpleat(String nomAplicacio, String nomModul, String nomPermis, int idEmpleat);
    void deleteEmpleat(String nomAplicacio, String nomModul, String nomPermis, int idEmpleat);
    List<Empleat> listAllEmpleatDelPermis(String nomAplicacio, String nomModul, String nomPermis);

}

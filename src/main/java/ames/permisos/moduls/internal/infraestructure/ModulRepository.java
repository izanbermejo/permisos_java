package ames.permisos.moduls.internal.infraestructure;

import ames.permisos.moduls.internal.domain.Modul;
import ames.permisos.organigrama.internal.domain.Empleat;
import ames.permisos.organigrama.internal.domain.Funcio;

import java.util.List;
import java.util.Optional;

public interface ModulRepository {

    List<Modul> list();
    List<Modul> obtenirModulsByAplicacio(String nomAplicacio);
    void save(String nomAplicacio, String nomModul, String descripcio);
    void delete(String nomAplicacio, String nomModul);
    Optional<Modul> find(String nomAplicacio, String nomModul);
    List<Funcio> listFuncioDelModul(String nomAplicacio, String nomModul);
    Optional<Funcio> findAssignacioFuncio(String nomAplicacio, String nomModul, Funcio funcio);
    void saveAssignacioFuncio(String nomAplicacio, String nomModul, Funcio funcio);
    void deleteFuncio(String nomAplicacio, String nomModul, Funcio funcio);
    List<Empleat> listEmpleatDelModul(String nomAplicacio, String nomModul);
    Optional<Empleat> findAssignacioEmpleat(String nomAplicacio, String nomModul, int idEmpleat);
    void saveAssignacioEmpleat(String nomAplicacio, String nomModul, int idEmpleat);
    void deleteEmpleat(String nomAplicacio, String nomModul, int idEmpleat);
    List<Empleat> listAllEmpleatDelModul(String nomAplicacio, String nomModul);

}

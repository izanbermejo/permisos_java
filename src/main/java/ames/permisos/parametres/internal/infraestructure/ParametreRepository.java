package ames.permisos.parametres.internal.infraestructure;

import ames.permisos.parametres.internal.domain.EmpleatParametre;
import ames.permisos.parametres.internal.domain.FuncioParametre;
import ames.permisos.parametres.internal.domain.Parametre;

import java.util.List;
import java.util.Optional;

public interface ParametreRepository {

    List<Parametre> obtenirParametresByAplicacio(String nomAplicacio);
    Optional<Parametre> find(String nomAplicacio, String nomParametre);
    void save(String nomAplicacio, String nomParametre, String descripcio);
    void delete(String nomAplicacio, String nomParametre);
    List<FuncioParametre> listFuncioDelParametre(String nomAplicacio, String nomParametre);
    Optional<FuncioParametre> findAssignacioFuncio(String nomAplicacio, String nomParametre, FuncioParametre funcioParametre);
    void saveAssignacioFuncio(String nomAplicacio, String nomParametre, FuncioParametre funcioParametre);
    void deleteFuncio(String nomAplicacio, String nomParametre, FuncioParametre funcioParametre);
    List<EmpleatParametre> listEmpleatDelParametre(String nomAplicacio, String nomParametre);
    Optional<EmpleatParametre> findAssignacioEmpleat(String nomAplicacio, String nomModul, int idEmpleat);
    void saveAssignacioEmpleat(String nomAplicacio, String nomModul, String valor, int idEmpleat);
    void deleteEmpleat(String nomAplicacio, String nomModul, int idEmpleat);
    List<EmpleatParametre> listAllEmpleatDelParametre(String nomAplicacio, String nomParametre);

}

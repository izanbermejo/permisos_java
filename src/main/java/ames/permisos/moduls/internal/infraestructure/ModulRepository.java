package ames.permisos.moduls.internal.infraestructure;

import ames.permisos.moduls.internal.domain.Modul;

import java.util.List;
import java.util.Optional;

public interface ModulRepository {

    List<Modul> obtenirModulsByAplicacio(String nomAplicacio);
    void save(String nomAplicacio, String nomModul, String descripcio);
    void delete(String nomAplicacio, String nomModul);
    Optional<Modul> find(String nomAplicacio, String nomModul);
}

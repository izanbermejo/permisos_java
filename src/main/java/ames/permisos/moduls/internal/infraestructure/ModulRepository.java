package ames.permisos.moduls.internal.infraestructure;

import ames.permisos.moduls.internal.domain.Modul;

import java.util.List;

public interface ModulRepository {

    List<Modul> obtenirModulsByAplicacio(String nomAplicacio);

}

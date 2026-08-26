package ames.permisos.parametres.internal.infraestructure;

import ames.permisos.parametres.internal.domain.Parametre;

import java.util.List;

public interface ParametreRepository {

    List<Parametre> obtenirParametresByAplicacio(String nomAplicacio);

}

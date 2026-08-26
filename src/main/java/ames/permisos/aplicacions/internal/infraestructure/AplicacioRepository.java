package ames.permisos.aplicacions.internal.infraestructure;

import ames.permisos.aplicacions.internal.domain.Aplicacio;

import java.util.Optional;

public interface AplicacioRepository {
    void save(String nomAplicacio, String descripcio);
    void delete(String nomAplicacio);
    boolean estaAssignada(String nomAplicacio);
    Optional<Aplicacio> find(String nomAplicacio);

}

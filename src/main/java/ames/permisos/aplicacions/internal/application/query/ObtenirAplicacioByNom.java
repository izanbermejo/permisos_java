package ames.permisos.aplicacions.internal.application.query;

import ames.permisos.aplicacions.internal.domain.Aplicacio;
import ames.permisos.aplicacions.internal.infraestructure.AplicacioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirAplicacioByNom {

    @Autowired AplicacioRepository aplicacioRepo;

    public Optional<Aplicacio> executar(String nomAplicacio) {
        return aplicacioRepo.find(nomAplicacio);
    }
}
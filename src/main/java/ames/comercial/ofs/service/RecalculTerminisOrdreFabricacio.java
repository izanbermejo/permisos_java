package ames.comercial.ofs.service;

import ames.comercial.ofs.internal.domain.OrdreFabricacio;
import ames.comercial.ofs.internal.domain.Termini;
import ames.comercial.ofs.internal.infraestructure.OrdreFabricacioRepository;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@Component
public class RecalculTerminisOrdreFabricacio {

    @Autowired OrdreFabricacioRepository ofRepo;
    @Autowired ObtenirPeriodeFabricacioEstandardNormalitzat obtenirPeriodeFabricacioEstandardNormalitzat;

    public List<Termini> executar(KeyArticleClient articleClient, long quantitatOf) {
        var terminis = terminisActuals(articleClient);

        // Quantitat pendent
        var quantitatPendent = terminis.stream().mapToLong(Termini::quantitatPendent).sum();

        // Quantitat pendent del nou plaç
        var quantitatPendentNouPlas = quantitatOf - quantitatPendent;

        // Creació del nou termini
        var dataFabricacio = LocalDate.now().plusDays(obtenirPeriodeFabricacioEstandardNormalitzat.executar()-1);
        var nouTermini = Termini.nou(quantitatPendentNouPlas, dataFabricacio);

        // Es retornen els terminis anteriors que no estan pendents amb el nou termini afegit
        var resultat = new ArrayList<>(terminis.stream()
                .filter(Termini::isPendent)
                .map(Termini::nouAnterior)
                .toList());
        resultat.add(nouTermini);
        return resultat;
    }

    private List<Termini> terminisActuals (KeyArticleClient articleClient) {
        return ofRepo.get(articleClient)
                .filter(Predicate.not(OrdreFabricacio::isAnulada))
                .map(OrdreFabricacio::terminis)
                .orElse(List.of());
    }

}

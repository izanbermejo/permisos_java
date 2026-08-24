package ames.comercial.ofs.internal.domain.service;

import ames.comercial.ofs.internal.domain.Termini;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CompararTerminis {

    List<Termini> terminis1;
    List<Termini> terminis2;

    public CompararTerminis(List<Termini> terminis1, List<Termini> terminis2) {
        this.terminis1 = terminis1;
        this.terminis2 = terminis2;
    }

    /**
     * Comparació de dos llistes de terminis. Primerament es filtren els que no tenen quantitats pendents
     * ja que es té en compte que es poden comparar terminis on s'han rebut quantitat de les OF's
     *
     * @return true si les dos llistes tenen les mateixes dates amb quantitat pendent, false altrament
     */
    public boolean executar() {
        var datesTermini1 = generarResumDatesQuantitatsSenseSeguretat(terminis1);
        var datesTermini2 = generarResumDatesQuantitatsSenseSeguretat(terminis2);
        // En cas que els terminis que no son de seguretat ja siguin diferents es retorna false
        return datesTermini1.equals(datesTermini2);
    }

    /**
     * Genera un Set d'strings que identifiquen la data i la quantitat pendent dels terminis que tenen
     * alguna quantitat pendent
     *
     * @param terminis Llistat de terminis
     * @return Conjunt d'String amb data#quantitatPendent
     */
    private Set<String> generarResumDatesQuantitatsSenseSeguretat(List<Termini> terminis) {
        return terminis.stream()
                .filter(Termini::isPendent)
                .map(t -> t.data() + "#" + t.quantitatPendent() + "#" + t.isStockSeguretat())
                .collect(Collectors.toSet());
    }

}

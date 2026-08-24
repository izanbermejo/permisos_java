package ames.comercial.comandes.service;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class CalcularDiaSortida implements ICalcularDiaSortida {

    @Override
    public LocalDate executar(LocalDate dataSolicitada, long diesTransit, String diesSortida) {
        // Resta dels dies de transit
        var resultat = dataSolicitada;

        // Si els dies de transit son negatius no s'han de restar els caps de setmana
        if (diesTransit < 0) {
            int diesRestats = 0;
            while (diesRestats > diesTransit) {
                resultat = resultat.minusDays(1);
                // Només es resta si no es cap de setmana
                if (resultat.getDayOfWeek() != DayOfWeek.SATURDAY && resultat.getDayOfWeek() != DayOfWeek.SUNDAY) {
                    diesRestats--;
                }
            }
        } else {
            resultat = dataSolicitada.minusDays(diesTransit);
        }

        // Normalització dels dies de sortida per a que de dilluns a divendres
        // sigui (XXXXX··) i no (·XXXXX·) que es com està guardat a BBDD
        // i també s'afegeixen els espais fins a omplir 7 caràcters
        var diesSortidaNorm = (diesSortida == null || diesSortida.isBlank() || diesSortida.length() < 2)
                ? "XXXXX  "
                : String.format("%-7s", diesSortida.substring(1));
        // Comprovació que hagi alguna X de dilluns a divendres (pot ser que a BBDD de l'Advantage només hagin espais
        // o altres caràcters no vàlids). En aquest cas que no hagi res valid ho deixem a (XXXXX··)
        diesSortidaNorm = !algunDiaValid(diesSortidaNorm)
                ? "XXXXX  "
                : diesSortidaNorm;
        // Comprovar si la data es vàlida
        var isValidDate = diesSortidaNorm.charAt(resultat.getDayOfWeek().getValue()-1) != ' ';
        while (!isValidDate) {
            resultat = resultat.minusDays(1);
            isValidDate = diesSortidaNorm.charAt(resultat.getDayOfWeek().getValue()-1) != ' ';
        }
        return resultat;
    }

    private boolean algunDiaValid(String diesSortida) {
        boolean result = false;
        for (int i = 0; i < 5; i++) { // lunes (0) a viernes (4)
            if (diesSortida.charAt(i) == 'X') {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public LocalDate executar(LocalDate dataSolicitada, long diesTransit) {
        return executar(dataSolicitada, diesTransit, "");
    }
}

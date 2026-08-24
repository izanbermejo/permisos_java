package ames.comercial.entrades.internal.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@JsonDeserialize(builder = InformacioSortidaEdiImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioSortidaEdi {

    int diesRestar();
    List<Integer> diesSortida();

    default LocalDate calcularDies(LocalDate dataSolicitada) {
        // Resta dels dies de trànsit
        LocalDate resultat = restarDiesTransit(dataSolicitada, diesRestar());

        // Normalització: si no hi ha dies vàlids, assumim de dilluns a divendres
        List<Integer> diesValids = (diesSortida() == null || diesSortida().isEmpty())
                ? List.of(1, 2, 3, 4, 5)
                : diesSortida();

        // Retrocedim fins trobar un dia de sortida vàlid
        while (!esDiaValid(resultat, diesValids)) {
            resultat = resultat.minusDays(1);
        }

        return resultat;
    }

    private LocalDate restarDiesTransit(LocalDate data, long diesRestar) {
        if (diesRestar >= 0) {
            return data.minusDays(diesRestar);
        }

        // Dies negatius: no compten caps de setmana
        LocalDate resultat = data;
        int diesRestats = 0;
        while (diesRestats > diesRestar) {
            resultat = resultat.minusDays(1);
            if (resultat.getDayOfWeek() != DayOfWeek.SATURDAY && resultat.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diesRestats--;
            }
        }
        return resultat;
    }

    private boolean esDiaValid(LocalDate data, List<Integer> diesSortida) {
        int diaSetmana = data.getDayOfWeek() == DayOfWeek.SUNDAY
                ? 7
                : data.getDayOfWeek().getValue();
        for (int dia : diesSortida) {
            if (dia == diaSetmana) return true;
        }
        return false;
    }
}

package ames.comercial.albarans.internal.application.command;

import ames.comercial.albarans.internal.domain.albara.Albara;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultat de la creació d'albarans (sortida o traspàs): la llista d'albarans afectats, distingint
 * els que s'han creat de nou dels que s'han aprofitat (albarans oberts als quals s'han afegit o
 * incrementat línies). El frontend el fa servir per mostrar el modal de resultat amb l'enllaç al
 * detall de cada albarà.
 */
public record ResultatCreacioAlbaransResponse(List<AlbaraResultat> albarans) {

    /** Construeix el resultat a partir dels albarans creats de nou i dels albarans oberts aprofitats */
    public static ResultatCreacioAlbaransResponse de(List<Albara> creats, List<Albara> aprofitats) {
        var albarans = new ArrayList<AlbaraResultat>();
        creats.forEach(a -> albarans.add(AlbaraResultat.de(a, false)));
        aprofitats.forEach(a -> albarans.add(AlbaraResultat.de(a, true)));
        return new ResultatCreacioAlbaransResponse(albarans);
    }

    public record AlbaraResultat(
            long codi,
            String empresa,
            /** Cert si l'albarà s'ha aprofitat (ja existia obert); fals si s'ha creat de nou */
            boolean aprofitat,
            LocalDate data,
            boolean tancat,
            String magatzem,
            String destinatari
    ) {
        public static AlbaraResultat de(Albara albara, boolean aprofitat) {
            return new AlbaraResultat(
                    albara.id().codi(),
                    albara.id().empresa(),
                    aprofitat,
                    albara.data(),
                    albara.isTancat(),
                    albara.magatzem(),
                    albara.adresa().destinatari());
        }
    }

}

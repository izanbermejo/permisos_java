package ames.comercial.edi2.internal.application.service;

import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.edi2.internal.domain.linia.LiniaEdiImpl;
import ames.comercial.edi2.internal.domain.linia.bloc.DA;
import ames.comercial.edi2.internal.domain.linia.bloc.DAImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NetejaLiniesEdiByFermService {

    /**
     * Neteja les línies EDI descomptant la quantitat total de les fermes del sistema.
     * És el mateix algorisme que el trànsit però aplicat a les línies EDI:
     * - Les línies EDI totalment consumides per les fermes no es retornen
     * - Les parcialment consumides es retornen amb la quantitat restant
     *
     * @param liniesEdi    Línies que vénen per EDI
     * @param totalFermes  Suma de quantitatActual de totes les FERMs del sistema
     * @return             Línies EDI netes (sense les consumides per les fermes)
     */
    public List<LiniaEdi> netejar(List<LiniaEdi> liniesEdi, long totalFermes) {
        List<LiniaEdi> resultat = new ArrayList<>();
        long restant = totalFermes;

        for (LiniaEdi linia : liniesEdi) {

            long qty = linia.quantitatNova();
            long consumit = Math.min(qty, restant);

            restant -= consumit;

            long novaQty = qty - consumit;

            // Si la línea ha sido consumida completamente por FERMES no se añade.
            // Si no había nada que consumir, se conserva.
            if (novaQty > 0 || consumit == 0) {
                resultat.add(rebuildLinia(linia, novaQty));
            }
        }

        return resultat;
    }

    private LiniaEdi rebuildLinia(LiniaEdi original, long novaQty) {

        DA novaDA = DAImpl.builder()
                .from(original.da())
                .cantidad(novaQty)
                .build();

        return LiniaEdiImpl.builder()
                .from(original)
                .da(novaDA)
                .build();
    }
}
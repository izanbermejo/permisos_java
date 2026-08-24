package ames.comercial.edi2.internal.application.service;

import ames.comercial.edi2.internal.domain.LiniaTransit;
import ames.comercial.edi2.internal.domain.LiniaTransitImpl;
import ames.comercial.edi2.internal.domain.LiniesDeute;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.edi2.internal.domain.linia.LiniaEdiImpl;
import ames.comercial.edi2.internal.domain.linia.bloc.DA;
import ames.comercial.edi2.internal.domain.linia.bloc.DAImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NetejaLiniesTransitService {

    public record NetejaLiniesTransitResponse(List<LiniaEdi> liniaEdis, List<LiniaTransit> liniesTransit, List<LiniesDeute> liniesDeutes) {}

    public NetejaLiniesTransitResponse aplicarTransit(List<LiniaEdi> liniesEdi, long pecesEnTransit, boolean isDuesDates) {
        List<LiniaEdi> resultat = new ArrayList<>();
        List<LiniaTransit> transit = new ArrayList<>();
        long restant = Math.abs(pecesEnTransit);
        long acumulat = 0;

        for (LiniaEdi linia : liniesEdi) {
            // Es passa la quantitat que ve per l'edi i la quantitat de peces en transit, s'agafa la de menor valor,
            // ja que, al anar restant la quantitat amb les peceses en transit es posible que no quadrin i en aquest cas
            // s'haura d'agafar la que vingui per l'edi
            long consumit = calcularConsum(linia.quantitatNova(), restant);
            restant -= consumit;

            // si el consumit es mes gran que 0 vol dir que queden peces en transit i que per tant cal anar afegint
            // la linia a list de transit
            if (consumit > 0) {
                acumulat += consumit;
                transit.add(buildLiniaTransit(linia, consumit, acumulat, Math.abs(pecesEnTransit), isDuesDates));
            }

            // per altra banda si la novaQuantitat es mes gran a 0, vol dir que al fer la resta encara queden peces de
            // l'edi per contar i que per tant ja no estan en transit
            long novaQty = linia.quantitatNova() - consumit;
            if (novaQty > 0 || restant == 0 && consumit == 0) {
                resultat.add(rebuildLinia(linia, novaQty));
            }
        }

        return new NetejaLiniesTransitResponse(resultat, transit, List.of());
    }

    private long calcularConsum(long qty, long restant) {
        return restant > 0 ? Math.min(qty, restant) : 0;
    }

    private LiniaTransit buildLiniaTransit(LiniaEdi linia, long consumit, long acumulat, long total, boolean isDuesDates) {
        return LiniaTransitImpl.builder()
                .dataSolicitada(linia.dataSortidaAMES(isDuesDates))
                .quantitat(consumit)
                .acumulat(acumulat)
                .total(total)
                .build();
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
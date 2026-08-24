package ames.comercial.edi2.internal.application.service;

import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.edi2.internal.domain.LiniaTransit;
import ames.comercial.edi2.internal.domain.LiniesDeute;
import ames.comercial.edi2.internal.domain.LiniesDeuteImpl;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
public class DeuteService {

    public record NetejaLiniesDeuteResponse(List<LiniaEdi> liniaEdis, List<LiniaTransit> liniesTransit, List<LiniesDeute> deuteList) {}

    public NetejaLiniesDeuteResponse saldarDeute(List<ObtenirLiniesComandaPendentsResponse> liniesSistema,
                                                 long pecesEndarrerides, LocalDate dataIniciEdi, String numComanda) {
        // Llista on acumularem les línies de deute generades
        List<LiniesDeute> deuteList = new ArrayList<>();

        // Convertim el deute a valor absolut (sempre treballem amb positiu)
        long restant = pecesEndarrerides;
        long acumulat = 0;
        // Filtrar només les línies anteriors a la data d'inici de l'EDI
        // (només aquestes poden absorbir el deute)
        List<ObtenirLiniesComandaPendentsResponse> liniesFiltradesSistema = liniesSistema.stream()
                .filter(l -> l.dataSolicitada().isBefore(dataIniciEdi.minusDays(1)))
                .filter(l -> Objects.equals(l.comandaClient(), numComanda))
                .sorted(Comparator.comparing(ObtenirLiniesComandaPendentsResponse::dataSolicitada))
                .toList();

        // Recorrem les línies del sistema per anar reduint el deute
        for (ObtenirLiniesComandaPendentsResponse linia : liniesFiltradesSistema) {
            // Si ja no queda deute, sortim
            if (restant <= 0) break;

            // Consumim el mínim entre el pendent de la línia i el deute restant
            long consumit = Math.min(linia.quantitatPendent(), restant);

            // Restem el consumit del deute
            restant -= consumit;
            acumulat += consumit;
            // Generem una línia de deute associada a la línia del sistema
            deuteList.add(buildLiniaDeute(linia.clauLinia(), linia.dataSolicitada(), consumit, acumulat, pecesEndarrerides));
        }

        // Si encara queda deute després de consumir totes les línies:
        // es crea una nova línia amb data d'avui
        if (restant > 0){
            acumulat += restant;
            deuteList.add(buildLiniaDeute(LocalDate.now(), restant, acumulat, pecesEndarrerides ));
        }

        // Retornem només les línies de deute (sense EDI ni trànsit en aquest cas)
        return new NetejaLiniesDeuteResponse(List.of(), List.of(), deuteList);
    }

    // Helper per crear una línia de deute sense clau de comanda (nova línia)
    private LiniesDeute buildLiniaDeute(LocalDate dataSolicitada, long consumit, long acumulat, long total) {
        return buildLiniaDeute(null, dataSolicitada, consumit, acumulat, total);
    }

    private LiniesDeute buildLiniaDeute(KeyLiniaComanda liniaComanda, LocalDate dataSolicitada, long consumit, long acumulat, long total) {
        return LiniesDeuteImpl.builder()
                // La clau pot ser null si és una línia nova generada
                .liniaComanda(Optional.ofNullable(liniaComanda))
                .dataSolicitada(dataSolicitada)
                .quantitat(consumit)
                .acumulat(acumulat)
                .total(total)
                .build();
    }
}

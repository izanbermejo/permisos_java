package ames.comercial.propostes.internal.service;

import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse.DatesPrimeraLiniaTraspas;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse.RegArticlesTraspasPendents;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse.ResumMagatzem;
import ames.comercial.propostes.internal.application.query.ResumMagatzemImpl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ConstruirResumMagatzems {

    public List<ResumMagatzem> build(List<RegArticlesTraspasPendents> traspassos) {
        return traspassos.stream()
                .collect(Collectors.groupingBy(
                        RegArticlesTraspasPendents::magEntrega,
                        Collectors.toList()
                )).entrySet().stream()
                .map(entry -> {
                    String magatzem = entry.getKey();
                    var items = entry.getValue();
                    String magatzemDesc = items.get(0).magEntregaDesc();

                    long total = items.size();
                    long ambStockSuficient = items.stream()
                            .filter(item -> item.qtatTraspassable() >= item.qtatTraspas())
                            .count();
                    long ambStockCap = items.stream()
                            .filter(item -> item.qtatTraspassable() == 0)
                            .count();
                    long ambStockParcial = total - ambStockCap - ambStockSuficient;

                    // Peces en estants sate·lits
                    var pecesSatelit = items.stream().anyMatch(r -> r.stockOrigenSatelit() > 0);
                    // Necessitat d'agafar peces d'estants satel·lits
                    var necessitaPecesSatelit = items.stream().anyMatch(RegArticlesTraspasPendents::necessitaStockSatelit);

                    // Propera necessitat d'stock del magatzem: article amb dataSortidaInterna més propera
                    var datesProperaNecessitatStock = properaNecessitatStock(items);

                    return ResumMagatzemImpl.builder()
                            .magatzem(magatzem)
                            .magatzemDesc(magatzemDesc)
                            .numTraspassos(total)
                            .numTraspassosAmbStockTot(ambStockSuficient)
                            .numTraspassosAmbStockParcial(ambStockParcial)
                            .numTraspassosAmbStockCap(ambStockCap)
                            .isHiHaPecesSatelit(pecesSatelit)
                            .isNecessitaPecesDeSatelit(necessitaPecesSatelit)
                            .datesProperaNecessitatStock(datesProperaNecessitatStock)
                            .build();
                })
                .sorted(Comparator.comparing(ResumMagatzem::magatzemDesc))
                .collect(Collectors.toList());
    }

    private Optional<DatesPrimeraLiniaTraspas> properaNecessitatStock(List<RegArticlesTraspasPendents> items) {
        return items.stream()
                .map(RegArticlesTraspasPendents::datesPrimeraLiniaTraspas)
                .flatMap(Optional::stream)
                .filter(d -> d.dataSortidaInterna().isPresent())
                .min(Comparator.comparing((DatesPrimeraLiniaTraspas d) -> d.dataSortidaInterna().orElse(LocalDate.MAX)));
    }

}

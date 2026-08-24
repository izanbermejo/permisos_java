package ames.comercial.comandes.service;

import ames.comercial.comandes.internal.domain.linia.DadesCalculNormalitzat;
import ames.comercial.comandes.internal.domain.linia.DadesCalculNormalitzatImpl;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComandaImpl;
import ames.comercial.comandes.service.response.CalculComandaNormalitzatResponse;
import ames.comercial.comandes.service.response.LiniaNormalitzatResp;
import ames.comercial.server.I18N;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CalculCanvisLiniesComanda {

    List<LiniaComanda> linies;
    CalculComandaNormalitzatResponse respCalc;

    public CalculCanvisLiniesComanda(List<LiniaComanda> linies, CalculComandaNormalitzatResponse respCalc) {
         this.linies = linies;
         this.respCalc = respCalc;
    }

    public CalculCanvisLiniesComandaResp calcula() {
        // Càlcul de les línies que han canviat de preu (només aquelles on no s'ha servit cap peça)
        List<LiniaComanda> liniesCanviPreu = new ArrayList<>();
        List<String> missatges = new ArrayList<>();
        List<String> missatgesServides = new ArrayList<>();
        var diferenciaImports = BigDecimal.ZERO;
        for (var liniaCalculada : respCalc.linies()) {
            var optLiniaOriginal = linies.stream()
                    .filter(l -> liniaCalculada.linia() == l.id().numero())
                    .findAny();
            // En el cas que s'afegeixi una línia no estàra present en les originals
            // però no canvia el preu i tampoc cal notificar-ho en els missatges
            if (optLiniaOriginal.isPresent()) {
                var liniaOriginal = optLiniaOriginal.get();
                // Comparació del preu i actualització del preu i les dades de càlcul de la línia
                var quantitatCalculOriginal = liniaOriginal.dadesCalcul().map(DadesCalculNormalitzat::quantitatCalcul).orElse(0L);
                if (liniaCalculada.quantitatCalcul() != quantitatCalculOriginal) {
                    var tePesesServides = liniaOriginal.quantitatServida() > 0;
                    // Només s'actualitza el preu de la línia en cas que s'hagin servit peces
                    if (!tePesesServides) {
                        liniesCanviPreu.add(updatePreu(liniaOriginal, liniaCalculada));
                        generaMissatge(liniaOriginal,liniaCalculada).ifPresent(missatges::add);
                    } else {
                        generaMissatgeServides(liniaOriginal, liniaCalculada).ifPresent(missatgesServides::add);
                        diferenciaImports = diferenciaImports.add(diferenciaImport(liniaOriginal, liniaCalculada));
                    }
                }
            }
        }
        if (!missatgesServides.isEmpty()) {
            missatgesServides.add("TOTAL: " + diferenciaImports);
        }

        return CalculCanvisLiniesComandaRespImpl.builder()
                .liniesCanviPreu(liniesCanviPreu)
                .missatges(missatges)
                .missatgesServides(missatgesServides)
                .build();
    }

    private LiniaComanda updatePreu(LiniaComanda liniaOriginal, LiniaNormalitzatResp liniaCalc) {
        return LiniaComandaImpl.builder()
                .from(liniaOriginal)
                .preu(liniaCalc.preu())
                .dadesCalcul(DadesCalculNormalitzatImpl.builder()
                        .descompte(liniaCalc.descompte())
                        .quantitatCalcul(liniaCalc.quantitatCalcul())
                        .build())
                .build();
    }

    private Optional<String> generaMissatge(LiniaComanda liniaOriginal, LiniaNormalitzatResp liniaCalc) {
        if (Preu.mateix(liniaOriginal.preu(), liniaCalc.preu()))
            return Optional.empty();
        var quantitatCalculOriginal = liniaOriginal.dadesCalcul().map(DadesCalculNormalitzat::quantitatCalcul).orElse(0L);
        var quantitatCalculActual = liniaCalc.quantitatCalcul();
        return Optional.of(I18N.getLiteral("comandes.canvipreulinia", liniaOriginal.numero(), liniaOriginal.preu().valor(), liniaOriginal.preu().divisa(), quantitatCalculOriginal,
                liniaCalc.preu().valor(), liniaCalc.preu().divisa(), quantitatCalculActual));
    }

    private Optional<String> generaMissatgeServides(LiniaComanda liniaOriginal, LiniaNormalitzatResp liniaCalc) {
        if (Preu.mateix(liniaOriginal.preu(), liniaCalc.preu()))
            return Optional.empty();
        var quantitatCalculOriginal = liniaOriginal.dadesCalcul().map(DadesCalculNormalitzat::quantitatCalcul).orElse(0L);
        var quantitatCalculActual = liniaCalc.quantitatCalcul();
        var diferenciaImport = diferenciaImport(liniaOriginal, liniaCalc);
        return Optional.of(I18N.getLiteral("comandes.canvipreuliniaservida", liniaOriginal.numero(), liniaOriginal.preu().valor(), liniaOriginal.preu().divisa(), quantitatCalculOriginal,
                liniaCalc.preu().valor(), liniaCalc.preu().divisa(), quantitatCalculActual, diferenciaImport, liniaOriginal.preu().divisa()));
    }

    private BigDecimal diferenciaImport(LiniaComanda liniaOriginal, LiniaNormalitzatResp liniaCalc) {
        var importOriginal = liniaOriginal.preu().imp(liniaOriginal.quantitatServida());
        var importNou = liniaCalc.preu().imp(liniaOriginal.quantitatServida());
        return importOriginal.subtract(importNou);
    }

    @JsonDeserialize(builder = CalculCanvisLiniesComandaRespImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CalculCanvisLiniesComandaResp {
        List<LiniaComanda> liniesCanviPreu();
        List<String> missatges();
        List<String> missatgesServides();
    }

}


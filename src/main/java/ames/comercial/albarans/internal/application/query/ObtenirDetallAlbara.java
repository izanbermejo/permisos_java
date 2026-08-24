package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.AlbaransException.AlbaraNoExisteix;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.albara.AlbaraRepository;
import ames.comercial.albarans.internal.infraestructure.linia.LiniaAlbaraRepository;
import ames.comercial.shared.Divisa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Carrega tota la informació d'un albarà (capçalera, adreça, estats, costos, observacions...)
 * juntament amb les seves línies i els totals. És la font de dades del modal de detall d'albarà del frontend.
 */
@Service
public class ObtenirDetallAlbara {

    @Autowired AlbaraRepository albaraRepository;
    @Autowired LiniaAlbaraRepository liniaAlbaraRepository;

    @Transactional(readOnly = true)
    public DetallAlbaraResponse executar(KeyAlbara idAlbara) {
        var albara = albaraRepository.find(idAlbara).orElseThrow(() -> new AlbaraNoExisteix(idAlbara));
        var linies = liniaAlbaraRepository.findByAlbara(idAlbara).stream()
                .sorted(Comparator.comparingLong(l -> l.id().linia()))
                .toList();
        return new DetallAlbaraResponse(albara, linies, TotalsAlbara.calcular(linies));
    }

    public record DetallAlbaraResponse(Albara albara, List<LiniaAlbara> linies, TotalsAlbara totals) {}

    /**
     * Totals de l'albarà calculats a partir de les seves línies. Es calculen aquí, i no al frontend,
     * per no replicar-hi les fórmules ni arrossegar l'arrodoniment de cada línia (mateix criteri que
     * {@code RecalcularImportPesLinia} al mòdul de propostes).
     */
    public record TotalsAlbara(BigDecimal pesNet,
                               BigDecimal importBrut,
                               BigDecimal importNet,
                               BigDecimal importDescompte,
                               Optional<Divisa> divisa) {

        static TotalsAlbara calcular(List<LiniaAlbara> linies) {
            // El pes es suma en grams i només es divideix un cop, per no acumular l'arrodoniment
            // a Kg que ja fa cada línia (LiniaAlbara.pesTotal()).
            var pesGrams = linies.stream()
                    .map(l -> l.informacioPesa().pesUnitari().multiply(BigDecimal.valueOf(l.quantitat())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            var pesNet = pesGrams.divide(BigDecimal.valueOf(1_000), 2, RoundingMode.HALF_UP);

            // Els imports es sumen a partir dels de cada línia (ja arrodonits a 2 decimals) perquè el
            // total quadri amb el que es veu columna a columna, el mateix criteri que el total del PDF.
            var importBrut = linies.stream()
                    .map(LiniaAlbara::importBrut)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            var importNet = linies.stream()
                    .map(LiniaAlbara::importNet)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

            // Divisa base dels imports: totes les línies d'un albarà són del mateix client i per tant
            // comparteixen divisa; les que estan en cèntims (%) ja les converteix Preu.imp() a la base.
            var divisa = linies.stream().findFirst().map(l -> l.preu().divisa().base());

            return new TotalsAlbara(pesNet, importBrut, importNet, importBrut.subtract(importNet), divisa);
        }

    }

}

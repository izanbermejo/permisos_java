package ames.comercial.albarans.internal.services;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.*;
import ames.comercial.albarans.internal.services.CalcularCreacioAlbaransSortida.CalcularCreacioAlbaransSortidaResponse.CreacioNovaLinia;
import ames.comercial.comandes.ext.IObtenirInformacioLiniesComanda.InformacioLiniaComandaDTO;
import ames.comercial.shared.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CreacioLiniesAlbara {

    KeyAlbara clauAlbara;
    Optional<Long> numeroLiniaAlbara = Optional.empty();

    public CreacioLiniesAlbara(KeyAlbara clauAlbara) {
        this.clauAlbara = clauAlbara;
    }

    public CreacioLiniesAlbara withNumeroLiniaAlbara(Long numeroLiniaAlbara) {
        this.numeroLiniaAlbara = Optional.of(numeroLiniaAlbara);
        return this;
    }

    public List<Pair<LiniaAlbara, List<InformacioLiniaComandaDTO>>> executar(List<CreacioNovaLinia> liniesAlbaraCrear) {
        List<Pair<LiniaAlbara, List<InformacioLiniaComandaDTO>>> resultat = new ArrayList<>();
        for (var l : liniesAlbaraCrear) {
            resultat.add(new Pair<>(crearLiniaAlbara(l), l.liniesComanda()));
        }
        return resultat;
    }

    private LiniaAlbara crearLiniaAlbara(CreacioNovaLinia liniaAlbaraCrear) {
        return LiniaAlbaraImpl.builder()
                .id(KeyLiniaAlbara.of(clauAlbara, nextNumeroLinia()))
                .articleClient(liniaAlbaraCrear.articleClient())
                .informacioPesa(crearInformacioPesa(liniaAlbaraCrear))
                .infoComanda(InformacioComanda.of(liniaAlbaraCrear.comanda(), liniaAlbaraCrear.comandaSegonsClient(), liniaAlbaraCrear.programa()))
                .quantitat(liniaAlbaraCrear.quantitatServir())
                .preu(liniaAlbaraCrear.preu())
                .isPreuFixat(liniaAlbaraCrear.isPreuFixat())
                .descompte(liniaAlbaraCrear.descompte())
                .quantitatPendentConsumir(0)
                .quantitatPendentFacturar(liniaAlbaraCrear.quantitatServir())
                .comandaBlanca(obtenirComandaBlanca(liniaAlbaraCrear.liniesComanda()))
                .build();
    }

    private InformacioPesa crearInformacioPesa(CreacioNovaLinia liniaAlbaraCrear) {
        // Obtenció d'una línia de comanda ja que conté l'informació de la matriu, referència, nivell tècnic i denominació que s'ha de copiar a la línia d'albarà.
        // Aquesta informació s'obté directament de l'articleclient per això és la mateixa a totes les línies agrupades
        var liniaComanda = liniaAlbaraCrear.liniesComanda().get(0);
        return InformacioPesaImpl.builder()
                .matriu(liniaComanda.projecte())
                .referencia(liniaComanda.referencia())
                .nivellTecnic(liniaComanda.nivellTecnic())
                .denominacio(liniaComanda.denominacio())
                .codiPartidaArantzelaria(liniaComanda.codiPartidaArantzelaria())
                .partidaArantzelaria(liniaComanda.partidaArantzelaria())
                .codiEan13(liniaComanda.codiEan13())
                .isVolUdi(liniaComanda.isVolUdi())
                .diesCaducitat(liniaComanda.diesCaducitat())
                .codiFamilia(liniaComanda.codiFamilia())
                .pesUnitari(liniaComanda.pesUnitari())
                .unitatsEmbalatge(liniaComanda.unitatsEmbalatge())
                .bossesCaixa(liniaComanda.bossesCaixa())
                .caixesPalet(liniaComanda.caixesPalet())
                .build();
    }

    private Optional<Long> obtenirComandaBlanca(List<InformacioLiniaComandaDTO> liniesComanda) {
        // Obtenció de totes les CB's de les línies de comanda agrupades.
        var listComandesBlanques = liniesComanda.stream()
                .map(InformacioLiniaComandaDTO::comandaBlanca)
                .flatMap(Optional::stream)
                .toList();
        // Si hi ha més d'una CB diferent vol dir que s'estan agrupant línies de comanda que pertanyen a diferents CB's i per tant no es pot assignar una CB concreta a la línia d'albarà
        // Això no hauria de passar mai ja que el procés de creació d'albarans hauria d'agrupat només línies de comanda que pertanyen a la mateixa CB però es posa aquesta comprovació
        // per assegurar la integritat de les dades
        if (listComandesBlanques.size() > 1)
            throw new IllegalStateException("No es pot crear una línia d'albarà agrupant línies de comanda que pertanyen a diferents comandes blanques");
        return listComandesBlanques.stream().findFirst();
    }

    private long nextNumeroLinia() {
        long numLinia = numeroLiniaAlbara.orElse(1L);
        this.numeroLiniaAlbara = Optional.of(numLinia+1);
        return numLinia;
    }

}

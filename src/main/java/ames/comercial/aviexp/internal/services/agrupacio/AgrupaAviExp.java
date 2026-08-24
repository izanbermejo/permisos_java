package ames.comercial.aviexp.internal.services.agrupacio;

import ames.comercial.advantage.IObtenirEtiquetesAlbara.EtiquetaTransportAlbara;
import ames.comercial.albarans.internal.domain.linia.InformacioComanda;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AgrupaAviExp {

    List<LiniaAlbara> liniesAlbara;
    List<EtiquetaTransportAlbara> etiquetesTransportAlbara;

    public AgrupaAviExp(List<LiniaAlbara> liniesAlbara, List<EtiquetaTransportAlbara> etiquetesTransportAlbara) {
        this.liniesAlbara = liniesAlbara;
        this.etiquetesTransportAlbara = etiquetesTransportAlbara;
    }

    public Map<ClauAgrupacioAviExp, AcumulatAviExp> agrupar() {
        Map<ClauAgrupacioAviExp, AcumulatAviExp> resultat = new LinkedHashMap<>();

        // Agrupació de les etiquetes de transport per línia de l'albarà. Ja que pot haber més d'una etiqueta de transport associada
        // a una línia de l'albarà (en cas que la línia de l'albarà faci referència a més d'una caixa)
        var mapaEtiquetesPerLiniaAlbara = etiquetesTransportAlbara.stream()
                .collect(Collectors.groupingBy(EtiquetaTransportAlbara::clauLiniaAlbara));

        for (var liniaAlbara : liniesAlbara) {
            // Etiquetes associades a la línia d'albarà
            var etiquetesAssociades = mapaEtiquetesPerLiniaAlbara.getOrDefault(liniaAlbara.id(), List.of());

            // Clau d'agrupació per a la línia d'albarà actual
            var comandaClient = liniaAlbara.infoComanda()
                    .map(InformacioComanda::comandaClient)
                    .orElse("");
            var clauAgrupacio = new ClauAgrupacioAviExp(liniaAlbara.articleClient(), comandaClient);

            // Recuperació de l'acumulat existent per a la clau d'agrupació actual o creació d'un acumulat buit si no existeix
            var acumulatActual = resultat.getOrDefault(clauAgrupacio, AcumulatAviExp.empty(liniaAlbara.id()));

            // Acumulació de les etiquetes associades a la línia d'albarà actual a l'acumulat existent
            var acumulatActualitzat = acumulatActual;
            for (var etiqueta : etiquetesAssociades) {
                acumulatActualitzat = acumulatActualitzat.acumular(etiqueta, liniaAlbara.id());
            }

            // Actualització del resultat amb l'acumulat actualitzat per a la clau d'agrupació actual
            resultat.put(clauAgrupacio, acumulatActualitzat);
        }

        return resultat;
    }


}

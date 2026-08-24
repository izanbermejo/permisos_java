package ames.comercial.albarans.internal.services.agrupacio.strategies;

import ames.comercial.albarans.internal.services.agrupacio.AgrupadorAlbara;
import ames.comercial.albarans.internal.services.agrupacio.ClauAgrupacio;
import ames.comercial.comandes.ext.IObtenirInformacioLiniesComanda.InformacioLiniaComandaDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AgruparPerComandaPesa implements AgrupadorAlbara {

    public Map<ClauAgrupacio, List<InformacioLiniaComandaDTO>> agrupar(List<InformacioLiniaComandaDTO> linies) {
        return linies.stream()
                .collect(Collectors.groupingBy(
                        l -> ClauAgrupacio.perComandaPesa(l.comandaSegonsClient(), l.articleClient(),l.empresa(), l.adresa(), l.informacioEnviament())
                ));
    }

}

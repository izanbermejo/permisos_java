package ames.comercial.albarans.internal.services.agrupacio;

import ames.comercial.comandes.ext.IObtenirInformacioLiniesComanda.InformacioLiniaComandaDTO;

import java.util.List;
import java.util.Map;

public interface AgrupadorAlbara {
    Map<ClauAgrupacio, List<InformacioLiniaComandaDTO>> agrupar(List<InformacioLiniaComandaDTO> linies);
}

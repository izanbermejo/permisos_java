package ames.comercial.albarans.internal.services.aviexp.ET;

import ames.comercial.albarans.internal.services.aviexp.AviExpUtils;
import ames.comercial.aviexp.internal.services.agrupacio.AcumulatAviExp;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.ElementEmbalatgeExpedicio;
import ames.comercial.edi2.internal.domain.linia.bloc.DA;

import java.util.Optional;

public class GenerarETTapaCaixa extends GenerarET {

    public GenerarETTapaCaixa(ElementEmbalatgeExpedicio embalatgeExpedicio, AcumulatAviExp acumulatAviExp, Optional<DA> da) {
        super(embalatgeExpedicio, acumulatAviExp, da);
    }

    @Override
    public String generar() {
        var sb = new StringBuilder();
        // Conjunt d'etiquetes de transport associades a l'agrupació ordenades per número d'etiqueta de transport
        var etiquetesTransport = acumulatAviExp.etiquetesTransportOrdenades();
        // En el cas de les tapes de caixes només es mostra una etiqueta de transport
        var etiquetaTransport = etiquetesTransport.iterator().next();
        // Inicio de registro
        sb.append(ET);
        // Num etiqueta nivel superior
        sb.append(AviExpUtils.espais(9));
        // Num etiqueta nivel actual
        sb.append(AviExpUtils.numeric(etiquetaTransport, 9));
        // Numero RAN
        sb.append(AviExpUtils.alfanumeric(numeroRan(), 35));
        // Numero lote
        sb.append(AviExpUtils.espais(20));
        // Numero tarjeta kanban
        sb.append(AviExpUtils.alfanumeric(numTarjetaKanban(), 35));
        // Fecha entrada en línea
        sb.append(AviExpUtils.espais(10));
        // Hora entrada en línea
        sb.append(AviExpUtils.espais(5));
        // Fecha produccion
        sb.append(AviExpUtils.espais(10));
        // Fecha caducidad
        sb.append(AviExpUtils.espais(10));
        // Filler
        sb.append(AviExpUtils.espais(35));
        // Salt de línea
        sb.append("\n");
        return sb.toString();
    }

}

package ames.comercial.albarans.internal.services.aviexp.ET;

import ames.comercial.aviexp.internal.services.agrupacio.AcumulatAviExp;
import ames.comercial.edi2.internal.domain.linia.bloc.DA;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.ElementEmbalatgeExpedicio;

import java.util.Optional;

public class GenerarETCaixa extends GenerarET {

    public GenerarETCaixa(ElementEmbalatgeExpedicio embalatgeExpedicio, AcumulatAviExp acumulatAviExp, Optional<DA> da) {
        super(embalatgeExpedicio, acumulatAviExp, da);
    }

    @Override
    public String generar() {
        var sb = new StringBuilder();
        // Conjunt d'etiquetes de transport associades a l'agrupació ordenades per número d'etiqueta de transport
        var etiquetesTransport = acumulatAviExp.etiquetesTransportOrdenades();
        // Per cada etiqueta de transport, obtenim les etiquetes de caixa associades a aquesta etiqueta de transport
        // ordenades per número d'etiqueta de caixa i per cada etiqueta de caixa obtenim el rang d'etiquetes de producció
        // En el cas dels palets mixtes el número d'etiqueta de transport és el mateix que el de caixa
        for (var etiquetaTransport : etiquetesTransport) {
            var etiquetesCaixa = acumulatAviExp.etiquetesProduccioOrdenades(etiquetaTransport);
            for (var etiquetaCaixa : etiquetesCaixa) {
                sb.append(generaET(etiquetaCaixa));
            }
        }
        return sb.toString();
    }

}

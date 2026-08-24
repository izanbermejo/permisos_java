package ames.comercial.albarans.internal.services.aviexp.ET;

import ames.comercial.advantage.IObtenirEtiquetesAlbara.EtiquetaTransportAlbara;
import ames.comercial.albarans.internal.services.aviexp.AviExpUtils;
import ames.comercial.aviexp.internal.services.agrupacio.AcumulatAviExp;
import ames.comercial.edi2.internal.domain.linia.bloc.DA;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.ElementEmbalatgeExpedicio;

import java.util.Optional;

public abstract class GenerarET {

    protected static final String ET = "ET";

    protected ElementEmbalatgeExpedicio embalatgeExpedicio;
    protected AcumulatAviExp acumulatAviExp;
    protected Optional<DA> da;

    public GenerarET(ElementEmbalatgeExpedicio embalatgeExpedicio, AcumulatAviExp acumulatAviExp, Optional<DA> da) {
        this.embalatgeExpedicio = embalatgeExpedicio;
        this.acumulatAviExp = acumulatAviExp;
        this.da = da;
    }

    public abstract String generar();

    protected String generaET(EtiquetaTransportAlbara etiquetaTransportAlbara) {
        var sb = new StringBuilder();
        for (long i = etiquetaTransportAlbara.etiquetaProduccioDesde(); i <= etiquetaTransportAlbara.etiquetaProduccioFins(); i++) {
            // Inicio de registro
            sb.append(ET);
            // Num etiqueta nivel superior
            sb.append(numEtiquetaNivellSuperior(etiquetaTransportAlbara));
            // Num etiqueta nivel actual
            sb.append(AviExpUtils.alfanumeric(i, 9));
            // Numero RAN
            sb.append(AviExpUtils.alfanumeric(numeroRan(), 35));
            // Numero lote
            sb.append(AviExpUtils.alfanumeric(etiquetaTransportAlbara.lot(), 20));
            // Numero tarjeta kanban
            sb.append(AviExpUtils.alfanumeric(numTarjetaKanban(), 35));
            // Fecha entrada en línea
            sb.append(AviExpUtils.espais(10));
            // Hora entrada en línea
            sb.append(AviExpUtils.espais(5));
            // Fecha produccion
            sb.append(AviExpUtils.data(etiquetaTransportAlbara.dataFabricacio()));
            // Fecha caducidad
            sb.append(AviExpUtils.espais(10));
            // Filler
            sb.append(AviExpUtils.espais(35));
            // Salt de línea
            sb.append("\n");
        }
        return sb.toString();
    }

    private String numEtiquetaNivellSuperior(EtiquetaTransportAlbara etiquetaTransportAlbara) {
        if (etiquetaTransportAlbara.isCaixaSuelta())
            return AviExpUtils.espais(9);
        return AviExpUtils.numeric(etiquetaTransportAlbara.etiquetaTransport(), 9);
    }

    protected String numeroRan() {
        return da.flatMap(DA::numeroRan)
                .orElse("");
    }

    protected String numTarjetaKanban() {
        return da.flatMap(DA::numTarjetaKanban)
                .orElse("");
    }

}

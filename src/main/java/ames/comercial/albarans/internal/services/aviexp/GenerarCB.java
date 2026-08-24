package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.edi2.internal.domain.comanda.bloc.LC;

public class GenerarCB {

    private static final String CB = "CB";

    private final Albara albara;
    private final String dunsEnviamentEmpresa;
    private final LC lc;
    private final String isoPais;

    public GenerarCB(Albara albara, LC lc, String dunsEnviamentEmpresa, String isoPais) {
        this.albara = albara;
        this.dunsEnviamentEmpresa = dunsEnviamentEmpresa;
        this.lc = lc;
        this.isoPais = isoPais;
    }

    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(CB);
        // Referencia albaran proveedor (núm albarà)
        sb.append(AviExpUtils.alfanumeric(albara.id().codi(), 35));
        // Referencia albaran cliente (DUNS enviament empresa)
        sb.append(AviExpUtils.alfanumeric(dunsEnviamentEmpresa, 35));
        // ID consignatario/planta
        sb.append(AviExpUtils.alfanumeric(lc.codigoConsignatario(), 35));
        // Nombre consignatario
        sb.append(AviExpUtils.alfanumeric(albara.adresa().destinatari(), 35));
        // Pais consignatario
        sb.append(AviExpUtils.alfanumeric(isoPais, 3));
        // Fecha de expedición-envío
        sb.append(AviExpUtils.data(albara.informacioMagatzem().dataEnviament().orElseThrow().toLocalDate()));
        // Hora de expedición-envío
        sb.append(AviExpUtils.hora(albara.informacioMagatzem().dataEnviament().orElseThrow().toLocalTime()));
        // Filler
        sb.append(AviExpUtils.espais(20));
        // Salt de línea
        sb.append("\n");
        return sb.toString();
    }

}

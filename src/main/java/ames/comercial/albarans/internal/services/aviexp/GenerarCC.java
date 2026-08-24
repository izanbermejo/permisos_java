package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.edi2.internal.domain.capsalera.CI;

public class GenerarCC {

    private static final String INICIO_REGISTRO = "CC";

    private final CI ci;
    private final String transportista;

    public GenerarCC(CI ci, String transportista) {
        this.ci = ci;
        this.transportista = transportista;
    }

    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(INICIO_REGISTRO);
        // ID. Comprador
        sb.append(AviExpUtils.alfanumeric(ci.idComprador(), 35));
        // ID. Proveedor
        sb.append(AviExpUtils.alfanumeric(ci.idProveedor(), 35));
        // ID. Expedidor
        sb.append(AviExpUtils.alfanumeric(ci.idExpedidor(), 35));
        // ID. Transportista
        sb.append(AviExpUtils.alfanumeric(transportista, 17));
        // ID. Num cuenta interno
        sb.append(AviExpUtils.alfanumeric(ci.numCuentaInternaProveedor().orElse(""), 20));
        // Filler
        sb.append(AviExpUtils.espais(36));
        // Salt de línea
        sb.append("\n");
        return sb.toString();
    }

}

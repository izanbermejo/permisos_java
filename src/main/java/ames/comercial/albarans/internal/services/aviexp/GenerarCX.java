package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.albarans.internal.domain.albara.Albara;

public class GenerarCX {

    private static final String INICIO_REGISTRO = "CX";

    private final Albara albara;

    public GenerarCX(Albara albara) {
        this.albara = albara;
    }

    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(INICIO_REGISTRO);
        // Matricula
        sb.append(AviExpUtils.alfanumeric(albara.informacioMagatzem().matricula().orElse(""), 35));
        // Matricula remolque
        sb.append(AviExpUtils.alfanumeric("12345BCN", 35));
        // Codi postal expedidor
        sb.append(AviExpUtils.alfanumeric("08980", 17));
        // Codi postal comprador
        sb.append(AviExpUtils.alfanumeric(albara.adresa().codiPostal(), 17));
        // Codi postal proveedor
        sb.append(AviExpUtils.alfanumeric("08980", 17));
        // Filler
        sb.append(AviExpUtils.espais(57));
        // Salt de línea
        sb.append("\n");
        return sb.toString();
    }

}

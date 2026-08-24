package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.albarans.internal.domain.albara.Albara;

public class GenerarCO {

    private static final String INICIO_REGISTRO = "CO";

    private final Albara albara;
    private final String paisDestinacio;

    public GenerarCO(Albara albara, String paisDestinacio) {
        this.albara = albara;
        this.paisDestinacio = paisDestinacio;
    }

    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(INICIO_REGISTRO);
        // Nombre comprador
        sb.append(AviExpUtils.alfanumeric(albara.adresa().destinatari(), 35));
        // Dirección comprador
        sb.append(AviExpUtils.alfanumeric(albara.adresa().adresa(), 35));
        // Localidad comprador
        sb.append(AviExpUtils.alfanumeric(albara.adresa().poblacio(), 35));
        // Provincia comprador
        sb.append(AviExpUtils.espais(35));
        // Pais comprador
        sb.append(AviExpUtils.alfanumeric(paisDestinacio, 3));
        // Filler
        sb.append(AviExpUtils.espais(35));
        // Salt de línea
        sb.append("\n");
        return sb.toString();
    }

}

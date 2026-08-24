package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.advantage.IObtenirEmpresesAds.EmpresaAds;

public class GenerarCP {

    private static final String INICIO_REGISTRO = "CP";

    private final EmpresaAds empresa;

    public GenerarCP(EmpresaAds empresa) {
        this.empresa = empresa;
    }

    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(INICIO_REGISTRO);
        // Nombre expedidor
        sb.append(AviExpUtils.alfanumeric(empresa.descripcio().toUpperCase(), 35));
        // Dirección expedidor
        sb.append(AviExpUtils.alfanumeric(empresa.adresaAlbara().toUpperCase(), 35));
        // Localidad expedidor
        sb.append(AviExpUtils.alfanumeric(empresa.poblacioAlbara().toUpperCase(), 35));
        // Provincia expedidor
        sb.append(AviExpUtils.alfanumeric(empresa.paisAlbara().toUpperCase(), 35));
        // Pais expedidor
        sb.append(AviExpUtils.alfanumeric(empresa.gateComp().toUpperCase(), 3));
        // Filler
        sb.append(AviExpUtils.espais(35));
        // Salt de línea
        sb.append("\n");
        return sb.toString();
    }

}

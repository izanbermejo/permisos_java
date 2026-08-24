package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.edi2.internal.domain.comanda.bloc.LI;

import java.util.Optional;

public class GenerarLZ {

    private static final String LZ = "LZ";

    private Optional<LI> optLi;

    public GenerarLZ(Optional<LI> optLi) {
        this.optLi = optLi;
    }

    /**
     * Antic AVIEXP_STANDARD_INDRA_LZ
     * @return Representació del bloc LZ
     */
    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(LZ);
        // Codigo revision diseño
        sb.append(AviExpUtils.espais(2));
        // Rev diseño
        sb.append(AviExpUtils.alfanumeric(optLi.flatMap(LI::revisionDiseno).orElse(""), 35));
        // Fecha rev dis
        sb.append(AviExpUtils.espais(10));
        // Cambio ing
        sb.append(AviExpUtils.espais(35));
        // Fecha cambio ing
        sb.append(AviExpUtils.espais(10));
        // Cantidad multi ref
        sb.append(AviExpUtils.numeric(0, 15));
        // Factura
        sb.append(AviExpUtils.espais(10));
        // Precio
        sb.append(AviExpUtils.numeric(0, 15));
        // Fecha factura
        sb.append(AviExpUtils.espais(10));
        // Filler
        sb.append(AviExpUtils.espais(36));
        // Salt de línia
        sb.append("\n");
        return sb.toString();
    }

}

package ames.comercial.albarans.internal.services.aviexp;

public class GenerarZZ {

    private static final String ZZ = "ZZ";

    private int numRegistres;

    public GenerarZZ(int numRegistres) {
        this.numRegistres = numRegistres;
    }

    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(ZZ);
        // Nº registros
        sb.append(AviExpUtils.numeric(numRegistres, 10));
        // Filler
        sb.append(AviExpUtils.espais(168));
        // Salt de línia
        sb.append("\n\n");
        return sb.toString();
    }

}

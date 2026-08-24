package ames.comercial.albarans.internal.services.aviexp.BA;

import ames.comercial.albarans.internal.services.aviexp.AviExpUtils;
import ames.comercial.aviexp.internal.services.agrupacio.AcumulatAviExp;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.ElementEmbalatgeExpedicio;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class GenerarBA {

    private static final String BA = "BA";

    protected ElementEmbalatgeExpedicio embalatgeExpedicio;
    protected AcumulatAviExp acumulatAviExp;
    protected BigDecimal pesPesa;

    public GenerarBA(ElementEmbalatgeExpedicio embalatgeExpedicio, AcumulatAviExp acumulatAviExp, BigDecimal pesPesa) {
        this.embalatgeExpedicio = embalatgeExpedicio;
        this.acumulatAviExp = acumulatAviExp;
        this.pesPesa = pesPesa;
    }

    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(BA);
        // Nivel embalaje
        sb.append(AviExpUtils.alfanumeric(nivellEmbalatge(), 1));
        // Categoria embalaje
        sb.append(AviExpUtils.alfanumeric(categoriaEmbalatge(), 1));
        // Numero embalajes iguales
        sb.append(AviExpUtils.numeric(numeroEmbalajesIguales(), 3));
        // Referencia embalaje proveedor
        sb.append(AviExpUtils.espais(22));
        // Referencia embalaje client
        sb.append(AviExpUtils.alfanumeric(embalatgeExpedicio.referencia(), 35));
        // Cantidad piezas por embalaje
        sb.append(AviExpUtils.numeric(cantidadPiezasPorEmbalaje(), 15));
        // Peso bruto x embalaje
        sb.append(AviExpUtils.numeric(calcularPesBrut().divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP), 15));
        // Peso neto x embalaje
        sb.append(AviExpUtils.numeric(calcularPesNet().divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP), 15));
        // Nº bultos x palet
        sb.append(AviExpUtils.numeric(numBultos(), 5));
        // Tipo embalaje
        sb.append(AviExpUtils.espais(35));
        // Filler
        sb.append(AviExpUtils.espais(31));
        // Salt de línea
        sb.append("\n");
        return sb.toString();
    }

    protected String nivellEmbalatge() {
        return String.valueOf(embalatgeExpedicio.tipus().nivell());
    }

    protected abstract String categoriaEmbalatge();

    protected abstract long numeroEmbalajesIguales();

    protected abstract long cantidadPiezasPorEmbalaje();

    protected abstract BigDecimal calcularPesNet();

    protected abstract BigDecimal calcularPesBrut();

    protected abstract long numBultos();

}

package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.edi2.internal.domain.comanda.bloc.LG;
import ames.comercial.edi2.internal.domain.comanda.bloc.LH;
import ames.comercial.edi2.internal.domain.comanda.bloc.LS;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class GenerarLR {

    private static final String LR = "LR";

    private BigDecimal pesBrut;
    private Optional<LH> lh;
    private Optional<LG> lg;
    private Optional<LS> ls;

    public GenerarLR(BigDecimal pesBrut, Optional<LH> lh, Optional<LG> lg, Optional<LS> ls) {
        this.pesBrut = pesBrut;
        this.lh = lh;
        this.lg = lg;
        this.ls = ls;
    }

    /**
     * Antic AVIEXP_STANDARD_INDRA_LR
     * @return Representació del bloc LR
     */
    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(LR);
        // Peso bruto línea
        sb.append(AviExpUtils.numeric(pesBrut.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP), 15));
        // Peso neto líniea (com no tenim el pes net posem el mateix que el pes brut)
        sb.append(AviExpUtils.numeric(pesBrut.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP), 15));
        // Numero lab
        sb.append(AviExpUtils.alfanumeric(lh.flatMap(LH::numeroLab).orElse("NUMLAB"), 35));
        // Numero contrato/pedido
        sb.append(AviExpUtils.alfanumeric(lg.map(LG::numeroContratoPedido).orElse(""), 35));
        // Fecha contrato
        sb.append(AviExpUtils.espais(10));
        // Id planta destino
        sb.append(AviExpUtils.alfanumeric(ls.flatMap(LS::codigoSolicitante).orElse("CODIGOSOLICITANTE"), 35));
        // Num linea contrato/pedido
        sb.append(AviExpUtils.espais(3));
        // Filler
        sb.append(AviExpUtils.espais(30));
        // Salt de línia
        sb.append("\n");
        return sb.toString();
    }

}

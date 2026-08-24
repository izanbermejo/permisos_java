package ames.comercial.albarans.internal.services.aviexp.BA;

import ames.comercial.aviexp.internal.services.agrupacio.AcumulatAviExp;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.ElementEmbalatgeExpedicio;

import java.math.BigDecimal;

public class GenerarBASeparadorCaixa extends GenerarBA {

    public GenerarBASeparadorCaixa(ElementEmbalatgeExpedicio embalatgeExpedicio, AcumulatAviExp acumulatAviExp, BigDecimal pesPesa) {
        super(embalatgeExpedicio, acumulatAviExp, pesPesa);
    }

    /**
     * El nivell d'embalatge del nivell 4 s'ha d'enviar al fitxer com a BA5
     * @return El nivell d'embalatge del nivell 4 s'ha d'enviar al fitxer com a BA5
     */
    @Override
    public String nivellEmbalatge() {
        return "5";
    }


    @Override
    protected String categoriaEmbalatge() {
        return "S";
    }

    @Override
    protected long numeroEmbalajesIguales() {
        return acumulatAviExp.numPalets();
    }

    @Override
    protected long cantidadPiezasPorEmbalaje() {
        return acumulatAviExp.totalPeces();
    }

    @Override
    protected BigDecimal calcularPesNet() {
        return BigDecimal.ZERO;
    }

    @Override
    protected BigDecimal calcularPesBrut() {
        return BigDecimal.ZERO;
    }

    @Override
    protected long numBultos() {
        return 1;
    }

}

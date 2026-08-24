package ames.comercial.albarans.internal.services.aviexp.BA;

import ames.comercial.aviexp.internal.services.agrupacio.AcumulatAviExp;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.ElementEmbalatgeExpedicio;
import ames.comercial.shared.Numbers;

import java.math.BigDecimal;

public class GenerarBACaixa extends GenerarBA {

    public GenerarBACaixa(ElementEmbalatgeExpedicio embalatgeExpedicio, AcumulatAviExp acumulatAviExp, BigDecimal pesPesa) {
        super(embalatgeExpedicio, acumulatAviExp, pesPesa);
    }

    @Override
    protected String categoriaEmbalatge() {
        return "S";
    }

    @Override
    protected long numeroEmbalajesIguales() {
        return acumulatAviExp.totalCaixes();
    }

    @Override
    protected long cantidadPiezasPorEmbalaje() {
        // S'obté la quantitat de peces per caixa de la primera etiqueta de transport, ja que totes les caixes tenen la mateixa quantitat de peces.
        return acumulatAviExp.etiquetes().get(0).quantitatPecesPerCaixa();
    }

    @Override
    protected BigDecimal calcularPesNet() {
        return pesPesa.multiply(Numbers.decimal(cantidadPiezasPorEmbalaje()));
    }

    @Override
    protected BigDecimal calcularPesBrut() {
        return calcularPesNet()
                .add(Numbers.decimal(40)); // Pes caixa 40g extret del codi antic Delphi
    }

    @Override
    protected long numBultos() {
        return 1;
    }

}

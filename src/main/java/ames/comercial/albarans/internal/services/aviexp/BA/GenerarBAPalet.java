package ames.comercial.albarans.internal.services.aviexp.BA;

import ames.comercial.aviexp.internal.services.agrupacio.AcumulatAviExp;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.ElementEmbalatgeExpedicio;
import ames.comercial.shared.Numbers;

import java.math.BigDecimal;

public class GenerarBAPalet extends GenerarBA {

    public GenerarBAPalet(ElementEmbalatgeExpedicio embalatgeExpedicio, AcumulatAviExp acumulatAviExp, BigDecimal pesPesa) {
        super(embalatgeExpedicio, acumulatAviExp, pesPesa);
    }

    @Override
    protected String categoriaEmbalatge() {
        return "M";
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
        return pesPesa.multiply(Numbers.decimal(acumulatAviExp.totalPeces()));
    }

    @Override
    protected BigDecimal calcularPesBrut() {
        var pesNet = calcularPesNet();
        var pesCaixes = Numbers.decimal(acumulatAviExp.totalCaixes() * 40);
        return pesNet
                .add(pesCaixes)
                .add(Numbers.decimal(15000)); // Pes palet 15Kg extret del codi antic Delphi
    }

    @Override
    protected long numBultos() {
        return acumulatAviExp.totalCaixes();
    }

}

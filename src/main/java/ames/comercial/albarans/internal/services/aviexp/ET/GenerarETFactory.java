package ames.comercial.albarans.internal.services.aviexp.ET;

import ames.comercial.aviexp.internal.services.agrupacio.AcumulatAviExp;
import ames.comercial.edi2.internal.domain.linia.bloc.DA;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.ElementEmbalatgeExpedicio;

import java.util.Optional;

public class GenerarETFactory {

    public static GenerarET crear(ElementEmbalatgeExpedicio embalatgeExpedicio,
                                  AcumulatAviExp acumulatAviExp,
                                  Optional<DA> optDA) {
        return switch (embalatgeExpedicio.tipus()) {

            case CAIXA -> new GenerarETCaixa(
                    embalatgeExpedicio,
                    acumulatAviExp,
                    optDA);

            case TAPA_CAIXA -> new GenerarETTapaCaixa(
                    embalatgeExpedicio,
                    acumulatAviExp,
                    optDA);

            default -> new GenerarETNoCaixa(
                    embalatgeExpedicio,
                    acumulatAviExp,
                    optDA);

        };
    }
}

package ames.comercial.albarans.internal.services.aviexp.BA;

import ames.comercial.aviexp.internal.services.agrupacio.AcumulatAviExp;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.ElementEmbalatgeExpedicio;

import java.math.BigDecimal;

public class GenerarBAFactory {

    public static GenerarBA crear(ElementEmbalatgeExpedicio embalatgeExpedicio,
                                  AcumulatAviExp acumulatAviExp,
                                  BigDecimal pesPesa) {
        return switch (embalatgeExpedicio.tipus()) {

            case CAIXA ->
                    new GenerarBACaixa(
                            embalatgeExpedicio,
                            acumulatAviExp,
                            pesPesa);

            case PALET ->
                    new GenerarBAPalet(
                            embalatgeExpedicio,
                            acumulatAviExp,
                            pesPesa);

            case TAPA_PALET ->
                    new GenerarBATapaPalet(
                            embalatgeExpedicio,
                            acumulatAviExp,
                            pesPesa);

            case SEPARADOR_CAIXA ->
                    new GenerarBASeparadorCaixa(
                            embalatgeExpedicio,
                            acumulatAviExp,
                            pesPesa);

            case TAPA_CAIXA ->
                    new GenerarBATapaCaixa(
                            embalatgeExpedicio,
                            acumulatAviExp,
                            pesPesa);

        };
    }
}

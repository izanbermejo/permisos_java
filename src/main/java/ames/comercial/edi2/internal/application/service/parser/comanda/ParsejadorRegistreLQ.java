package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LQ;
import ames.comercial.edi2.internal.domain.comanda.bloc.LQImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLQ extends ExtreureTipusDada {

    public static LQ parse(String linia) {
        validarLiniaLQ(linia);

        return LQImpl.builder()
                .inicio(extreureText(linia, 0, i_inicio))
                .cantidadBalance(extreureLongOptional(linia, i_inicio, i_cantidadBalance))
                .fechaCantidadBalance(extreureDateOptional(linia, i_cantidadBalance, i_fechaCantidadBalance))
                .cantidadAtraso(extreureLongOptional(linia, i_fechaCantidadBalance, i_cantidadAtraso))
                .fechaCantidadAtraso(extreureDateOptional(linia, i_cantidadAtraso, i_fechaCantidadAtraso))
                .cantidadUrgente(extreureLongOptional(linia, i_fechaCantidadAtraso, i_cantidadUrgente))
                .fechaCantidadUrgente(extreureDateOptional(linia, i_cantidadUrgente, i_fechaCantidadUrgente))
                .cantidadTransito(extreureLongOptional(linia, i_fechaCantidadUrgente, i_cantidadTransito))
                .fechaCantidadTransito(extreureDateOptional(linia, i_cantidadTransito, i_fechaCantidadTransito))
                .cantidadAcumuladaRecibida(extreureLongOptional(linia, i_fechaCantidadTransito, i_cantidadAcumuladaRecibida))
                .cantidadAcumuladaProgramada(extreureLongOptional(linia, i_cantidadAcumuladaRecibida, i_cantidadAcumuladaProgramada))
                .inicioPeriodoAcumulada(extreureDateOptional(linia, i_cantidadAcumuladaProgramada, i_inicioPeriodoAcumulada))
                .finPeriodoAcumulada(extreureDateOptional(linia, i_inicioPeriodoAcumulada, i_finPeriodoAcumulada))
                .cantidadAcumuladaPeriodoAnterior(extreureLongOptional(linia, i_finPeriodoAcumulada, i_cantidadAcumuladaPeriodoAnterior))
        .build();
    }

    private static void validarLiniaLQ(String linia) {
        if (linia == null || !linia.startsWith("LQ")) {
            throw new IllegalArgumentException("La línia no és un registre LQ vàlid");
        }
    }

    static int i_inicio = 2;
    static int i_cantidadBalance = 17;
    static int i_fechaCantidadBalance = 27;
    static int i_cantidadAtraso = 42;
    static int i_fechaCantidadAtraso = 52;
    static int i_cantidadUrgente = 67;
    static int i_fechaCantidadUrgente = 77;
    static int i_cantidadTransito = 92;
    static int i_fechaCantidadTransito = 102;
    static int i_cantidadAcumuladaRecibida = 117;
    static int i_cantidadAcumuladaProgramada = 132;
    static int i_inicioPeriodoAcumulada = 142;
    static int i_finPeriodoAcumulada = 152;
    static int i_cantidadAcumuladaPeriodoAnterior = 167;
}

package ames.comercial.edi2.internal.application.service.parser.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.CB;
import ames.comercial.edi2.internal.domain.capsalera.CBImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreCB extends ExtreureTipusDada {

    public static CB parse(String linia) {
        validarLiniaCB(linia);

        return CBImpl.builder()
            .inicioRegistro(extreureText(linia, 0, i_inicio))
            .fechaMensaje(extreureDate(linia, i_inicio, i_fechaMensaje))
            .horaMensaje(extreureHoraOptional(linia, i_fechaMensaje, i_horaMensaje))
            .fechaInicioHorizonte(extreureDateOptional(linia, i_horaMensaje, i_fechaInicioHorizonte))
            .fechaFinalHorizonte(extreureDateOptional(linia, i_fechaInicioHorizonte, i_fechaFinalHorizonte))
            .tipoFecha(extreureText(linia, i_fechaFinalHorizonte, i_tipoFecha))
            .idTransportista(extreureText(linia, i_tipoFecha, i_idTransportista))
            .tipoTransporte(extreureText(linia, i_idTransportista, i_tipoTransporte))
            .build();
    }

    private static void validarLiniaCB(String linia) {
        if (linia == null || !linia.startsWith("CB")) {
            throw new IllegalArgumentException("La línia no és un registre CB vàlid");
        }
    }

    static int i_inicio = 2;
    static int i_fechaMensaje = 12;
    static int i_horaMensaje = 17;
    static int i_fechaInicioHorizonte = 33;
    static int i_fechaFinalHorizonte = 49;
    static int i_tipoFecha = 50;
    static int i_idTransportista = 85;
    static int i_tipoTransporte = 120;
}

package ames.comercial.edi2.internal.application.service.parser.linia;

import ames.comercial.edi2.internal.domain.linia.bloc.DR;
import ames.comercial.edi2.internal.domain.linia.bloc.DRImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;

public class ParsejadorRegistreDR extends ExtreureTipusDada{

    public static DR parse(String linia) {
        validarLiniaDR(linia);

        return DRImpl.builder()
                .fechaEntradaLinea(extreureDateOptional(linia, i_inicio, i_fechaEntradaEnlinea))
                .horaEntradaLinea(extreureHoraOptional(linia, i_fechaEntradaEnlinea, i_horaEntradaEnlinea))
                .build();
    }

    private static void validarLiniaDR(String linia) {
        if (linia == null || !linia.startsWith("DR")) {
            throw new IllegalArgumentException("La línia no és un registre DR vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_fechaEntradaEnlinea = 12;
    public static final int i_horaEntradaEnlinea = 17;

}

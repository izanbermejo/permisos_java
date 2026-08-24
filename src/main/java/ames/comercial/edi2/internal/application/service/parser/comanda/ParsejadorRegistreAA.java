package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.AA;
import ames.comercial.edi2.internal.domain.comanda.bloc.AAImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreAA extends ExtreureTipusDada {

    public static AA parse(String linia) {
        validarLiniaAA(linia);

        return AAImpl.builder()
                .inicio(extreureText(linia, 0, i_inicio))
                .referenciaAlbaranEntrada(extreureText(linia, i_inicio, i_referenciaAlbaranEntrada))
                .fechaAlbaran(extreureDateOptional(linia, i_referenciaAlbaranEntrada, i_fechaAlbaran))
                .horaAlbaran(extreureHoraOptional(linia, i_fechaAlbaran, i_horaAlbaran))
                .cantidadEnviadaAlbaran(extreureText(linia, i_horaAlbaran, i_cantidadEnviadaAlbaran))
                .cantidadRecibidaAlbaran(extreureText(linia, i_cantidadEnviadaAlbaran, i_cantidadRecibidaAlbaran))
                .fechaRecepcion(extreureDateOptional(linia, i_cantidadRecibidaAlbaran, i_fechaRecepcion))
                .build();
    }

    private static void validarLiniaAA(String linia) {
        if (linia == null || !linia.startsWith("AA")) {
            throw new IllegalArgumentException("La línia no és un registre AA vàlid");
        }
    }

    public static int i_inicio = 2;
    public static int i_referenciaAlbaranEntrada = 37;
    public static int i_fechaAlbaran = 47;
    public static int i_horaAlbaran = 52;
    public static int i_cantidadEnviadaAlbaran = 68;
    public static int i_cantidadRecibidaAlbaran = 84;
    public static int i_fechaRecepcion = 94;
}

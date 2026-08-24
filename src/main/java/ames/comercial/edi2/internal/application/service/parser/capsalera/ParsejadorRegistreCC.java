package ames.comercial.edi2.internal.application.service.parser.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.CC;
import ames.comercial.edi2.internal.domain.capsalera.CCImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreCC extends ExtreureTipusDada {

    public static CC parse(String linia) {
        validarLiniaCC(linia);

        return CCImpl.builder()
            .inicioRegistro(extreureText(linia, 0, i_inicio))
            .nombreComprador(extreureText(linia, i_inicio, i_nombreComprador))
            .direccionComprador(extreureText(linia, i_nombreComprador, i_direccionComprador))
            .localidadComprador(extreureText(linia, i_direccionComprador, i_localidadComprador))
            .provinciaComprador(extreureText(linia, i_localidadComprador, i_provinciaComprador))
            .codigoPostalComprador(extreureText(linia, i_provinciaComprador, i_codigoPostalComprador))
            .build();
    }

    private static void validarLiniaCC(String linia) {
        if (linia == null || !linia.startsWith("CC")) {
            throw new IllegalArgumentException("La línia no és un registre CA vàlid");
        }
    }

    public static int i_inicio = 2;
    public static int i_nombreComprador = 37;
    public static int i_direccionComprador = 72;
    public static int i_localidadComprador = 107;
    public static int i_provinciaComprador = 132;
    public static int i_codigoPostalComprador = 142;
}

package ames.comercial.edi2.internal.application.service.parser.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.CT;
import ames.comercial.edi2.internal.domain.capsalera.CTImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreCT extends ExtreureTipusDada {

    public static CT parse(String linia) {
        validarLiniaCT(linia);

        return CTImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .textoLibre(extreureText(linia, i_inicio, i_textoLibre))
                .build();
    }

    private static void validarLiniaCT(String linia) {
        if (linia == null || !linia.startsWith("CT")) {
            throw new IllegalArgumentException("La línia no és un registre CA vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_textoLibre = 37;
}

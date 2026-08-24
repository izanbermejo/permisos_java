package ames.comercial.edi2.internal.application.service.parser.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.CF;
import ames.comercial.edi2.internal.domain.capsalera.CFImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreCF extends ExtreureTipusDada {

    public static CF parse(String linia) {
        validarLiniaCF(linia);

        return CFImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .nombreFacturacion(extreureText(linia, i_inicio, i_nombreFacturacion))
                .departamentoFacturacion(extreureText(linia, i_nombreFacturacion, i_departamentFacturacion))
                .build();
    }

    private static void validarLiniaCF(String linia) {
        if (linia == null || !linia.startsWith("CF")) {
            throw new IllegalArgumentException("La línia no és un registre CF vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_nombreFacturacion = 37;
    public static final int i_departamentFacturacion = 72;
}

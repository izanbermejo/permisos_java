package ames.comercial.edi2.internal.application.service.parser.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.CD;
import ames.comercial.edi2.internal.domain.capsalera.CDImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreCD extends ExtreureTipusDada {

    public static CD parse(String linia) {
        validarLiniaCD(linia);

        return CDImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .paisComprador(extreureText(linia, i_inicio, i_paisComprador))
                .personaContactoComprador(extreureText(linia, i_paisComprador, i_personaContactoComprador))
                .telefonoComprador(extreureText(linia, i_personaContactoComprador, i_telefonoComprador))
                .faxComprador(extreureText(linia, i_telefonoComprador, i_faxComprador))
                .emailComprador(extreureText(linia, i_faxComprador, i_emailComprador))
                .build();
    }

    private static void validarLiniaCD(String linia) {
        if (linia == null || !linia.startsWith("CD")) {
            throw new IllegalArgumentException("La línia no és un registre CA vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_paisComprador = 5;
    public static final int i_personaContactoComprador = 40;
    public static final int i_telefonoComprador = 75;
    public static final int i_faxComprador = 110;
    public static final int i_emailComprador = 145;

}

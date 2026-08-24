package ames.comercial.edi2.internal.application.service.parser.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.CQ;
import ames.comercial.edi2.internal.domain.capsalera.CQImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreCQ extends ExtreureTipusDada {

    public static CQ parse(String linia) {
        validarLiniaCQ(linia);

        return CQImpl.builder()
            .inicioRegistro(extreureText(linia, 0, i_inicio))
            .paisProveedor(extreureText(linia, i_inicio, i_paisProveedor))
            .personaContactoProveedor(extreureText(linia, i_paisProveedor, i_personaContactoProveedor))
            .telefonoProveedor(extreureText(linia, i_personaContactoProveedor, i_telefonoProveedor))
            .faxProveedor(extreureText(linia, i_telefonoProveedor, i_faxProveedor))
            .build();
    }

    private static void validarLiniaCQ(String linia) {
        if (linia == null || !linia.startsWith("CQ")) {
            throw new IllegalArgumentException("La línia no és un registre CA vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_paisProveedor = 5;
    public static final int i_personaContactoProveedor = 40;
    public static final int i_telefonoProveedor = 75;
    public static final int i_faxProveedor = 110;

}

package ames.comercial.edi2.internal.application.service.parser.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.CI;
import ames.comercial.edi2.internal.domain.capsalera.CIImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreCI extends ExtreureTipusDada {

    public static CI parse(String linia) {
        validarLiniaCI(linia);

        return CIImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .idComprador(extreureText(linia, i_inicio, i_idComprador))
                .idProveedor(extreureText(linia, i_idComprador, i_idProveedor))
                .numCuentaInternaProveedor(extreureText(linia, i_idProveedor, i_numCuentaInternaProveedor))
                .idExpedidor(extreureText(linia, i_numCuentaInternaProveedor, i_idExpedidor))
                .idFacturado(extreureText(linia, i_idExpedidor, i_idFacturado))
                .build();
    }

    private static void validarLiniaCI(String linia) {
        if (linia == null || !linia.startsWith("CI")) {
            throw new IllegalArgumentException("La línia no és un registre CA vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_idComprador = 37;
    public static final int i_idProveedor = 72;
    public static final int i_numCuentaInternaProveedor = 107;
    public static final int i_idExpedidor = 142;
    public static final int i_idFacturado = 177;

}

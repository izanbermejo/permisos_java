package ames.comercial.edi2.internal.application.service.parser.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.CP;
import ames.comercial.edi2.internal.domain.capsalera.CPImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreCP extends ExtreureTipusDada {

    public static CP parse(String linia) {
        validarLiniaCP(linia);

        return CPImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .nombreProveedor(extreureText(linia, i_inicio, i_nombreProveedor))
                .direccionProveedor(extreureText(linia, i_nombreProveedor, i_direccionProveedor))
                .localidadProveedor(extreureText(linia, i_direccionProveedor, i_localidadProveedor))
                .provinciaProveedor(extreureText(linia, i_localidadProveedor, i_provinciaProveedor))
                .codPostalProveedor(extreureText(linia, i_provinciaProveedor, i_codigoPostalProveedor))
                .build();
    }

    private static void validarLiniaCP(String linia) {
        if (linia == null || !linia.startsWith("CP")) {
            throw new IllegalArgumentException("La línia no és un registre CA vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_nombreProveedor = 37;
    public static final int i_direccionProveedor = 72;
    public static final int i_localidadProveedor = 107;
    public static final int i_provinciaProveedor = 142;
    public static final int i_codigoPostalProveedor = 152;

}

package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LB;
import ames.comercial.edi2.internal.domain.comanda.bloc.LBImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLB extends ExtreureTipusDada {

    public static LB parse(String linia) {
        validarLiniaLB(linia);

        return LBImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .stockActual(extreureLongOptional(linia, i_inicio, i_stockActual))
                .stockSeguridad(extreureLongOptional(linia, i_stockActual, i_stockSeguridad))
                .identificacionArticuloProveedor(extreureText(linia, i_stockSeguridad, i_idArticuloProveedor))
                .paisConsignatario(extreureText(linia, i_idArticuloProveedor, i_paisConsignatario))
                .precio(extreureBigDecimalOptional(linia, i_paisConsignatario, i_precio))
                .divisa(extreureText(linia, i_precio, i_divisa))
                .build();
    }

    private static void validarLiniaLB(String linia) {
        if (linia == null || !linia.startsWith("LB")) {
            throw new IllegalArgumentException("La línia no és un registre LB vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_stockActual = 17;
    public static final int i_stockSeguridad = 32;
    public static final int i_idArticuloProveedor = 67;
    public static final int i_paisConsignatario = 70;
    public static final int i_precio = 85;
    public static final int i_divisa = 88;

}

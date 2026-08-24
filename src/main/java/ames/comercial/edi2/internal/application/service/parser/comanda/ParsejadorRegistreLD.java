package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LD;
import ames.comercial.edi2.internal.domain.comanda.bloc.LDImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLD extends ExtreureTipusDada {

    public static LD parse(String linia) {
        validarLiniaLD(linia);

        return LDImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .direccion(extreureText(linia, i_inicio, i_direccionConsignatario))
                .localidad(extreureText(linia, i_direccionConsignatario, i_localidadConsignatario))
                .provincia(extreureText(linia, i_localidadConsignatario, i_provinciaConsignatario))
                .codigoPostal(extreureText(linia, i_provinciaConsignatario, i_codigoPostalConsignatario))
                .fax(extreureText(linia, i_codigoPostalConsignatario, i_faxConsignatario))
                .build();
    }

    private static void validarLiniaLD(String linia) {
        if (linia == null || !linia.startsWith("LD")) {
            throw new IllegalArgumentException("La línia no és un registre LD vàlid");
        }
    }

    public static final int i_inicio=2;
    public static final int i_direccionConsignatario=37;
    public static final int i_localidadConsignatario=72;
    public static final int i_provinciaConsignatario=97;
    public static final int i_codigoPostalConsignatario=107;
    public static final int i_faxConsignatario=142;

}

package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LC;
import ames.comercial.edi2.internal.domain.comanda.bloc.LCImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLC extends ExtreureTipusDada {

    public static LC parse(String linia) {
        validarLiniaLC(linia);

        return LCImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .codigoConsignatario(extreureText(linia, i_inicio, i_codigoConsignatario))
                .nombreConsignatario(extreureText(linia, i_codigoConsignatario, i_nombreConsignatario))
                .personaContacto(extreureText(linia, i_nombreConsignatario, i_personaContactoConsignatario))
                .telefono(extreureText(linia, i_personaContactoConsignatario, i_telefonoConsignatario))
                .build();
    }

    private static void validarLiniaLC(String linia) {
        if (linia == null || !linia.startsWith("LC")) {
            throw new IllegalArgumentException("La línia no és un registre LC vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_codigoConsignatario = 37;
    public static final int i_nombreConsignatario = 72;
    public static final int i_personaContactoConsignatario = 107;
    public static final int i_telefonoConsignatario = 142;

}

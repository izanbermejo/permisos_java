package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LT;
import ames.comercial.edi2.internal.domain.comanda.bloc.LTImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLT extends ExtreureTipusDada {

    public static LT parse(String linia) {
        validarLiniaLT(linia);

        return LTImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .texto1(extreureText(linia, i_inicio, i_texto1))
                .texto2(extreureText(linia, i_texto1, i_texto2))
                .texto3(extreureText(linia, i_texto2, i_texto3))
                .texto4(extreureText(linia, i_texto3, i_texto4))
                .build();
    }

    private static void validarLiniaLT(String linia) {
        if (linia == null || !linia.startsWith("LT")) {
            throw new IllegalArgumentException("La línia no és un registre LT vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_texto1 = 42;
    public static final int i_texto2 = 82;
    public static final int i_texto3 = 122;
    public static final int i_texto4 = 162;

}

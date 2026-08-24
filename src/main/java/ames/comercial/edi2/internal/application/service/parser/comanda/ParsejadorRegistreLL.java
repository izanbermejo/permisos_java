package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LL;
import ames.comercial.edi2.internal.domain.comanda.bloc.LLImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLL extends ExtreureTipusDada {

    public static LL parse(String linia) {
        validarLiniaLL(linia);

        return LLImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .codigoEtiqueta(extreureText(linia, i_inicio, i_codigoEtiqueta))
                .texto(extreureText(linia, i_codigoEtiqueta, i_texto))
                .build();
    }

    private static void validarLiniaLL(String linia) {
        if (linia == null || !linia.startsWith("LL")) {
            throw new IllegalArgumentException("La línia no és un registre LL vàlid");
        }
    }

    public static final  int i_inicio = 2;
    public static final  int i_codigoEtiqueta = 19;
    public static final  int i_texto = 54;

}

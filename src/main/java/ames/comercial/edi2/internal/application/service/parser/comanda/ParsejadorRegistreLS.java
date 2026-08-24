package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LS;
import ames.comercial.edi2.internal.domain.comanda.bloc.LSImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLS extends ExtreureTipusDada {

    public static LS parse(String linia) {
        validarLiniaLS(linia);

        return LSImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .codigoSolicitante(extreureText(linia, i_inicio, i_codigoSolicitante))
                .nombreSolicitante(extreureText(linia, i_codigoSolicitante, i_nombreSolicitante))
                .personaContacto(extreureText(linia, i_nombreSolicitante, i_personaAprovisionamiento))
                .telefono(extreureText(linia, i_personaAprovisionamiento, i_telefonoSolicitante))
                .build();
    }

    private static void validarLiniaLS(String linia) {
        if (linia == null || !linia.startsWith("LS")) {
            throw new IllegalArgumentException("La línia no és un registre LS vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_codigoSolicitante = 37;
    public static final int i_nombreSolicitante = 72;
    public static final int i_personaAprovisionamiento = 107;
    public static final int i_telefonoSolicitante = 142;

}

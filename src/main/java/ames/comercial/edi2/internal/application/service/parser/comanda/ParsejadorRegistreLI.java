package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LI;
import ames.comercial.edi2.internal.domain.comanda.bloc.LIImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLI extends ExtreureTipusDada {

    public static LI parse(String linia) {
        validarLiniaLI(linia);

        return LIImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .revisionDiseno(extreureText(linia, i_inicio, i_cambioIngenieria))
                .fechaCambioIngenieria(extreureDateOptional(linia, i_cambioIngenieria, i_fechaCambioIngerieria))
                .numeroRuta(extreureText(linia, i_fechaCambioIngerieria, i_numeroRuta))
                .sufijoRuta(extreureText(linia, i_numeroRuta, i_numeroSufijoRuta))
                .numeroTransporte(extreureText(linia, i_numeroSufijoRuta, i_numeroTransporte))
                .build();
    }

    private static void validarLiniaLI(String linia) {
        if (linia == null || !linia.startsWith("LI")) {
            throw new IllegalArgumentException("La línia no és un registre LI vàlid");
        }
    }

    public static final int i_inicio=2;
    public static final int i_cambioIngenieria=37;
    public static final int i_fechaCambioIngerieria=47;
    public static final int i_numeroRuta=82;
    public static final int i_numeroSufijoRuta=117;
    public static final int i_numeroTransporte=152;

}

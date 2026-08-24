package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LE;
import ames.comercial.edi2.internal.domain.comanda.bloc.LEImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLE extends ExtreureTipusDada {

    public static LE parse(String linia) {
        validarLiniaLE(linia);

        return LEImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .tipoBulto(extreureText(linia, i_inicio, i_tipoBulto))
                .referenciaEmbalajeCliente(extreureText(linia, i_tipoBulto, i_referenciaEmalaje))
                .cantPiezasPorEmbalaje(extreureLongOptional(linia, i_referenciaEmalaje, i_piezasPorEmbalaje))
                .tipoContenedor(extreureText(linia, i_piezasPorEmbalaje, i_tipoContenedor))
                .numeroEmbalajes(extreureLongOptional(linia, i_tipoContenedor, i_numeroEmbalajes))
                .nivelEmpaquetamiento(extreureText(linia, i_numeroEmbalajes, i_nivelEmpaquetamiento))
                .build();
    }

    private static void validarLiniaLE(String linia) {
        if (linia == null || !linia.startsWith("LE")) {
            throw new IllegalArgumentException("La línia no és un registre LE vàlid");
        }
    }

    public static final int i_inicio=2;
    public static final int i_tipoBulto=37;
    public static final int i_referenciaEmalaje=72;
    public static final int i_piezasPorEmbalaje=87;
    public static final int i_tipoContenedor=122;
    public static final int i_numeroEmbalajes=137;
    public static final int i_nivelEmpaquetamiento=140;

}

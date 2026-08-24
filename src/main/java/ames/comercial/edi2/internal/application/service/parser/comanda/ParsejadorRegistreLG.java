package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LG;
import ames.comercial.edi2.internal.domain.comanda.bloc.LGImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLG extends ExtreureTipusDada {

    public static LG parse(String linia) {
        validarLiniaLG(linia);

        return LGImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .descripcionArticulo(extreureText(linia, i_inicio, i_descripcionArticulo))
                .numeroContratoPedido(extreureText(linia, i_descripcionArticulo, i_numeroContrato))
                .fechaContratoPedido(extreureDateOptional(linia, i_numeroContrato, i_fechaContrato))
                .numeroDocumentoAnterior(extreureText(linia, i_fechaContrato, i_numeroDocumentoAnterior))
                .fechaDocumentoAnterior(extreureDateOptional(linia, i_numeroDocumentoAnterior, i_fechaDocumentoAnterior))
                .numeroLineaContrato(extreureOpcional(linia, i_fechaDocumentoAnterior, i_numeroLineaContrato))
                .numeroPlano(extreureText(linia, i_numeroLineaContrato, i_numeroDeplano))
                .build();
    }

    private static void validarLiniaLG(String linia) {
        if (linia == null || !linia.startsWith("LG")) {
            throw new IllegalArgumentException("La línia no és un registre LG vàlid");
        }
    }

    public static final int i_inicio=2;
    public static final int i_descripcionArticulo=37;
    public static final int i_numeroContrato=72;
    public static final int i_fechaContrato=82;
    public static final int i_numeroDocumentoAnterior=117;
    public static final int i_fechaDocumentoAnterior=127;
    public static final int i_numeroLineaContrato=133;
    public static final int i_numeroDeplano=168;

}

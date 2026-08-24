package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LH;
import ames.comercial.edi2.internal.domain.comanda.bloc.LHImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLH extends ExtreureTipusDada {

    public static LH parse(String linia) {
        validarLiniaLH(linia);

        return LHImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .numPedidoPrevio(extreureText(linia, i_inicio, i_numeroPedidoPrevio))
                .numeroLab(extreureText(linia, i_numeroPedidoPrevio, i_numeroLab))
                .numeroLote(extreureText(linia, i_numeroLab, i_numeroLote))
                .fechaLab(extreureDateOptional(linia, i_numeroLote, i_fechaLab))
                .fechaPedidoPrevio(extreureDateOptional(linia, i_fechaLab, i_fechaPedidoPrevio))
                .numeroPedidoNuevo(extreureText(linia, i_fechaPedidoPrevio, i_numeroPedidoNuevo))
                .fechaPedidoNuevo(extreureDateOptional(linia, i_numeroPedidoNuevo, i_fechaPedidoNuevo))
                .build();
    }

    private static void validarLiniaLH(String linia) {
        if (linia == null || !linia.startsWith("LH")) {
            throw new IllegalArgumentException("La línia no és un registre LH vàlid");
        }
    }

    public static final int i_inicio=2;
    public static final int i_numeroPedidoPrevio=37;
    public static final int i_numeroLab=72;
    public static final int i_numeroLote=107;
    public static final int i_fechaLab=117;
    public static final int i_fechaPedidoPrevio=127;
    public static final int i_numeroPedidoNuevo=162;
    public static final int i_fechaPedidoNuevo=172;

}

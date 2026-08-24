package ames.comercial.edi2.internal.application.service.parser.comanda;

import ames.comercial.edi2.internal.domain.comanda.bloc.LA;
import ames.comercial.edi2.internal.domain.comanda.bloc.LAImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreLA extends ExtreureTipusDada {

    public static LA parse(String linia) {
        validarLiniaLA(linia);

        return LAImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .idArticuloComprador(extreureText(linia, i_inicio, i_idArticuloComprador))
                .estadoArticulo(extreureText(linia, i_idArticuloComprador, i_estadoArticulo))
                .codigoAccion(extreureText(linia, i_estadoArticulo, i_codigoAccion))
                .unidadMedida(extreureText(linia, i_codigoAccion, i_unidadMedida))
                .lugarEntrega(extreureText(linia, i_unidadMedida, i_lugarEntrega))
                .lugarDestinoFinal(extreureText(linia, i_lugarEntrega, i_lugarDestinoFinal))
                .codigoAlmacen(extreureText(linia, i_lugarDestinoFinal, i_codigoAlmacen))
                .fechaCubierta(extreureDateOptional(linia, i_codigoAlmacen, i_fechaCubierta))
                .paisOrigenCodificado(extreureOpcional(linia, i_fechaCubierta, i_paisOrigenCodificado))
                .fechaLimiteEntrega(extreureDateOptional(linia, i_paisOrigenCodificado, i_fechaLimiteEntrega))
                .fechaCalculoActual(extreureDateOptional(linia, i_fechaLimiteEntrega, i_fechaCalculoActual))
                .fechaInicioCalculo(extreureDateOptional(linia, i_fechaCalculoActual, i_fechaInicioCalculo))
                .codigoFrecuenciaEntrega(extreureText(linia, i_fechaInicioCalculo, i_codigoFrecuenciaEntrega))
                .indicadorRequerimiento(extreureText(linia, i_codigoFrecuenciaEntrega, i_indicadorRequerimiento))
                .codigoCaracteristicaItem(extreureText(linia, i_indicadorRequerimiento, i_codigoCaracteristicaItem))
                .horaLimiteEntrega(extreureHoraOptional(linia, i_codigoCaracteristicaItem, i_horaLimiteEntrega))
                .build();
    }

    private static void validarLiniaLA(String linia) {
        if (linia == null || !linia.startsWith("LA")) {
            throw new IllegalArgumentException("La línia no és un registre LA vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_idArticuloComprador = 37;
    public static final int i_estadoArticulo = 42;
    public static final int i_codigoAccion = 43;
    public static final int i_unidadMedida = 46;
    public static final int i_lugarEntrega = 63;
    public static final int i_lugarDestinoFinal = 80;
    public static final int i_codigoAlmacen = 97;
    public static final int i_fechaCubierta = 107;
    public static final int i_paisOrigenCodificado = 110;
    public static final int i_fechaLimiteEntrega = 120;
	public static final int i_fechaCalculoActual = 130;
    public static final int i_fechaInicioCalculo = 140;
    public static final int i_codigoFrecuenciaEntrega = 141;
    public static final int i_indicadorRequerimiento = 142;
    public static final int i_codigoCaracteristicaItem = 144;
    public static final int i_horaLimiteEntrega = 149;

}

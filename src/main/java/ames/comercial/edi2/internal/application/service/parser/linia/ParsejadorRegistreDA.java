package ames.comercial.edi2.internal.application.service.parser.linia;

import ames.comercial.edi2.internal.domain.linia.bloc.DA;
import ames.comercial.edi2.internal.domain.linia.bloc.DAImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreDA extends ExtreureTipusDada {

    public static DA parse(String linia) {
        validarLiniaDA(linia);

        return DAImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .tipoDetalle(extreureText(linia, i_inicio, i_tipo))
                .cantidad(extreureLong(linia, i_tipo, i_cantidad))
                .unidadMedida(extreureText(linia, i_cantidad, i_unidadMedida))
                .fechaInicial(extreureDateOptional(linia, i_unidadMedida, i_fechaInicial))
                .horaInicial(extreureHoraOptional(linia, i_fechaInicial, i_horaInicial))
                .fechaFinal(extreureDateOptional(linia, i_horaInicial, i_fechaFinal))
                .horaFinal(extreureHoraOptional(linia, i_fechaFinal, i_horaFinal))
                .razonInstruccion(extreureOpcional(linia, i_horaFinal, i_razonInstruccion))
                .numeroRan(extreureOpcional(linia, i_razonInstruccion, i_numeroRAN))
                .fechaRan(extreureDateOptional(linia, i_numeroRAN, i_fechaRAN))
                .frecuenciaEnvio(extreureOpcional(linia, i_fechaRAN, i_frecuenciaEnvio))
                .numTarjetaKanban(extreureOpcional(linia, i_frecuenciaEnvio, i_numeroTarjetaKanban))
                .ultimoNumeroRanEmitido(extreureOpcional(linia, i_numeroTarjetaKanban, i_ultimoNumeroKanban))
                .build();
    }

    private static void validarLiniaDA(String linia) {
        if (linia == null || !linia.startsWith("DA")) {
            throw new IllegalArgumentException("La línia no és un registre DA vàlid");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_tipo = 3;
    public static final int i_cantidad = 20;
    public static final int i_unidadMedida = 23;
    public static final int i_fechaInicial = 33;
    public static final int i_horaInicial = 38;
    public static final int i_fechaFinal = 48;
    public static final int i_horaFinal = 53;
    public static final int i_razonInstruccion = 54;
    public static final int i_numeroRAN = 89;
    public static final int i_fechaRAN = 99;
	public static final int i_frecuenciaEnvio = 102;
    public static final int i_numeroTarjetaKanban = 137;
    public static final int i_ultimoNumeroKanban = 150;

}

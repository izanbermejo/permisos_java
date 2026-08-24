package ames.comercial.edi2.internal.application.service.parser.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.CA;
import ames.comercial.edi2.internal.domain.capsalera.CAImpl;
import ames.comercial.edi2.internal.utils.ExtreureTipusDada;
import org.springframework.stereotype.Component;

@Component
public class ParsejadorRegistreCA extends ExtreureTipusDada {

    public static CA parse(String linia) {
        validarLiniaCA(linia);

        return CAImpl.builder()
                .inicioRegistro(extreureText(linia, 0, i_inicio))
                .tipoMensaje(extreureText(linia, i_inicio, i_tipo))
                .numeroEnvio(extreureText(linia, i_tipo, i_numeroEnvio))
                .buzonOrigen(extreureText(linia, i_numeroEnvio, i_buzonOrigen))
                .buzonDestino(extreureText(linia, i_buzonOrigen, i_buzonDestino))
                .numeroDocumento(extreureText(linia, i_buzonDestino, i_numeroDocumento))
                .codigoDocumento(extreureText(linia, i_numeroDocumento, i_codigoDocumento))
                .documento(extreureText(linia, i_codigoDocumento, i_documento))
                .logisticaNombreMensaje(extreureOpcional(linia, i_documento, i_logistica))
                .funcionMensaje(extreureOpcional(linia, i_logistica, i_funcion))
                .referenciaAplicacion(extreureOpcional(linia, i_funcion, i_referencia))
                .build();
    }

    private static void validarLiniaCA(String linia) {
        if (linia == null || !linia.startsWith("CA")) {
            throw new IllegalArgumentException("La línia no és un registre CA vàlid");
        }
        if (linia.length() < i_referencia) {
            throw new IllegalArgumentException("Longitud incorrecta per a un registre CA");
        }
    }

    public static final int i_inicio = 2;
    public static final int i_tipo = 12;
    public static final int i_numeroEnvio = 26;
    public static final int i_buzonOrigen = 61;
    public static final int i_buzonDestino = 96;
    public static final int i_numeroDocumento = 131;
    public static final int i_codigoDocumento = 141;
    public static final int i_documento = 151;
    public static final int i_logistica = 161;
    public static final int i_funcion = 164;
    public static final int i_referencia = 178;
//	public static final int i_filler = 180;
}

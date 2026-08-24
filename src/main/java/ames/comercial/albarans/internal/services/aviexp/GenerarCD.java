package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.InformacioEdi;
import ames.comercial.edi2.internal.domain.comanda.bloc.LA;

import java.math.BigDecimal;

public class GenerarCD {

    private static final String INICIO_REGISTRO = "CD";

    private final Albara albara;
    private final LA la;
    private final BigDecimal pesNetExpedicio;

    public GenerarCD(Albara albara, LA la, BigDecimal pesNetExpedicio) {
        this.albara = albara;
        this.la = la;
        this.pesNetExpedicio = pesNetExpedicio;
    }

    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(INICIO_REGISTRO);
        // Lugar entrega
        sb.append(AviExpUtils.alfanumeric(lugarEntrega(), 17));
        // Codigo almacén
        sb.append(AviExpUtils.alfanumeric(codigoAlmacen(), 17));
        // Lugar destino final
        sb.append(AviExpUtils.alfanumeric(lugarDestinoFinal(), 17));
        // Peso bruto expedición
        sb.append(pesBrut());
        // Peso neto expedición
        sb.append(pesNet());
        // Unidad de medida del peso
        sb.append(AviExpUtils.alfanumeric("KGM", 3));
        // Filler
        sb.append(AviExpUtils.espais(94));
        // Salt de línea
        sb.append("\n");
        return sb.toString();
    }

    private String lugarEntrega() {
        return la.lugarEntrega()
                .filter(s -> !s.isBlank())
                .or(() -> albara.informacioEdi()
                        .map(InformacioEdi::aclgat)
                        .filter(s -> !s.isBlank()))
                .or(() -> albara.informacioEdi()
                        .map(InformacioEdi::punto)
                        .filter(s -> !s.isBlank()))
                .orElse("");
    }

    private String codigoAlmacen() {
        return la.codigoAlmacen()
                .filter(s -> !s.isBlank())
                .or(() -> albara.informacioEdi()
                        .map(InformacioEdi::punto)
                        .filter(s -> !s.isBlank()))
                .orElse("");
    }

    private String lugarDestinoFinal() {
        return la.lugarDestinoFinal()
                .filter(s -> !s.isBlank())
                .orElse(albara.informacioEnviament().desti());
    }

    private String pesBrut() {
        var pes = albara.informacioMagatzem().pesBrut().orElse(0L);
        var pesKg = pes * 1000;
        return AviExpUtils.numeric(pesKg, 15);
    }

    private String pesNet() {
        var pes = pesNetExpedicio.longValue();
        return AviExpUtils.numeric(pes, 15);
    }

}

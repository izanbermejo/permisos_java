package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.edi2.internal.domain.capsalera.CA;
import ames.comercial.edi2.internal.domain.comanda.bloc.LA;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class GenerarCA {

    private static final String INICIO_REGISTRO = "CA";
    private static final String ALBARAN = "ALBARAN";
    private static final String BUZON_ORIGEN = "0941A081182340";

    private final Albara albara;
    private final CA ca;
    private final LA la;
    private final LocalDateTime dataActual = LocalDateTime.now();

    public GenerarCA(Albara albara, CA ca, LA la) {
        this.albara = albara;
        this.ca = ca;
        this.la = la;
    }

    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(INICIO_REGISTRO);
        // Tipo de mensaje
        sb.append(ALBARAN);
        // Número de envío
        sb.append(AviExpUtils.espais(14));
        // TODO El buzón origen hauria de ser un valor configurable a nivell de magatzem o empresa. Per ara es igual a tots i es deixa fixe com a constant
        // Buzón origen
        sb.append(AviExpUtils.alfanumeric(BUZON_ORIGEN, 35));
        // Buzón destino (posem la bustia origen del missatge quan es va rebre per part del client)
        sb.append(AviExpUtils.alfanumeric(ca.buzonOrigen(), 35));
        // Número mensaje (núm albarà)
        sb.append(AviExpUtils.alfanumeric(albara.id().codi(), 35));
        // Fecha mensaje
        sb.append(AviExpUtils.data(dataActual.toLocalDate()));
        // Hora mensaje
        sb.append(AviExpUtils.hora(dataActual.toLocalTime()));
        // Fecha estimada de llegada
        sb.append(AviExpUtils.data(calcularFechaLlegada()));
        // Hora estimada de llegada
        sb.append(AviExpUtils.hora(calcularHoraLlegada()));
        // Logística / Razón de la instrucción
        sb.append(AviExpUtils.espais(1));
        // Referència de aplicación
        sb.append(AviExpUtils.alfanumeric("DESADV", 21));
        // Salt de línea
        sb.append("\n");
        return sb.toString();
    }

    private LocalDate calcularFechaLlegada() {
        if (la.fechaLimiteEntrega().isPresent())
            return la.fechaLimiteEntrega().get();
        return albara.informacioMagatzem()
                .dataPrevista()
                .map(LocalDateTime::toLocalDate)
                .orElse(dataActual.toLocalDate());
    }

    private LocalTime calcularHoraLlegada() {
        if (la.horaLimiteEntrega().isPresent())
            return la.horaLimiteEntrega().get();
        return albara.informacioMagatzem()
                .dataPrevista()
                .map(LocalDateTime::toLocalTime)
                .orElse(dataActual.toLocalTime());
    }

}

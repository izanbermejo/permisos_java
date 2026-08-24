package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.edi2.internal.domain.comanda.bloc.LI;
import ames.comercial.shared.FormaEnviament;

import java.util.Optional;

public class GenerarCT {

    private static final String INICIO_REGISTRO = "CT";

    private final Albara albara;
    private final Optional<LI> li;
    private final String descripcioTransportista;

    public GenerarCT(Albara albara, Optional<LI> li, String descripcioTransportista) {
        this.albara = albara;
        this.li = li;
        this.descripcioTransportista = descripcioTransportista;
    }

    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(INICIO_REGISTRO);
        // Nombre transportista
        sb.append(AviExpUtils.alfanumeric(descripcioTransportista, 35));
        // Tipo de transporte
        sb.append(AviExpUtils.alfanumeric(albara.informacioEnviament().incoterm().toString(), 3));
        // Modo de transporte
        sb.append(AviExpUtils.alfanumeric(modoTransporte(albara.informacioEnviament().formaEnviament()), 3));
        // Número de ruta / Nota de transporte
        sb.append(AviExpUtils.alfanumeric(li.flatMap(LI::numeroRuta)
                .or(() -> li.flatMap(LI::numeroTransporte))
                .orElse(""), 35));
        // Pago transporte
        sb.append(AviExpUtils.espais(3));
        // Tipo documento / descripción
        sb.append(tipoDocumentoDescripcion());
        // Filler
        sb.append(AviExpUtils.espais(30));
        // Salt de línea
        sb.append("\n");
        return sb.toString();
    }

    private String modoTransporte(FormaEnviament formaEnviament) {
        return switch (formaEnviament) {
            case VAIXELL -> "10";
            case TREN -> "20";
            case AVIO, COURIER_AERI -> "40";
            case CORREU -> "50";
            default -> "30";
        };
    }

    private String tipoDocumentoDescripcion() {
        // En cas que hagi informació EDI i el camp MRN no estigui buit, es posarà el número MRN, la data i el tipus.
        // En cas contrari, es deixaran 69 espais en blanc
        if (albara.informacioEdi().isPresent()) {
            var infoEdi = albara.informacioEdi().get();
            if (!infoEdi.mrnNum().isBlank()) {
                return AviExpUtils.alfanumeric(infoEdi.mrnNum(), 20)
                        + AviExpUtils.data(infoEdi.mrnData())
                        + AviExpUtils.alfanumeric(infoEdi.mrnType(), 2);
            }
        }
        return AviExpUtils.espais(69);
    }

}

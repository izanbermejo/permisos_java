package ames.comercial.albarans.internal.services.aviexp;

import ames.comercial.edi2.internal.domain.comanda.bloc.LA;

import java.util.Optional;

public class GenerarLA {

    private static final String REGISTRO_LA = "LA";

    private long quantitat;
    private long quantitatAcumulada;
    private String referenciaArticle;
    private String denominacioArticle;
    private String paisReferencia;
    private Optional<LA> la;

    public GenerarLA(long quantitat, long quantitatAcumulada, String referenciaArticle,
                     String denominacioArticle, String paisReferencia, Optional<LA> la) {
        this.quantitat = quantitat;
        this.quantitatAcumulada = quantitatAcumulada;
        this.referenciaArticle = referenciaArticle;
        this.denominacioArticle = denominacioArticle;
        this.paisReferencia = paisReferencia;
        this.la = la;
    }

    /**
     * Antic AVIEXP_STANDARD_INDRA_LA
     * @return Representació del bloc LA
     */
    public String generar() {
        var sb = new StringBuilder();
        // Inicio de registro
        sb.append(REGISTRO_LA);
        // Identificación artículo comprador
        sb.append(AviExpUtils.alfanumeric(la.map(LA::idArticuloComprador).orElse(referenciaArticle), 35));
        // Identificación artículo proveedor
        sb.append(AviExpUtils.espais(22));
        // Descripción artículo
        sb.append(AviExpUtils.alfanumeric(denominacioArticle, 35));
        // País referencia
        sb.append(AviExpUtils.alfanumeric(paisReferencia, 3));
        // Cantidad artículo
        sb.append(AviExpUtils.numeric(quantitat, 15));
        // Cantidad acumulada
        sb.append(AviExpUtils.numeric(quantitatAcumulada, 15));
        // Unidad medida
        sb.append(AviExpUtils.alfanumeric("PCE", 3));
        // Nivel configuración
        sb.append(AviExpUtils.espais(3));
        // Preference status
        sb.append(AviExpUtils.espais(1));
        // Bonded gods
        sb.append(AviExpUtils.espais(1));
        // Enginering change
        sb.append(AviExpUtils.espais(1));
        // Código característica del ITEM (Obligatorio para VW)
        sb.append(AviExpUtils.alfanumeric(codigoCaracteristicaItem(), 2));
        // Referencia albaran articulo
        sb.append(AviExpUtils.espais(35));
        // Filler
        sb.append(AviExpUtils.espais(7));
        // Salt de línia
        sb.append("\n");
        return sb.toString();
    }

    private String codigoCaracteristicaItem() {
        return la.
                flatMap(LA::codigoCaracteristicaItem)
                .filter(c -> !c.isBlank())
                .orElse("63");
    }

}

package ames.comercial.entrades.internal.application.service;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.advantage.internal.auxiliar.statements.MultipleInsertStatement;
import ames.comercial.entrades.internal.domain.EntradaMagatzem;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemEmbalatge;

import java.time.LocalDate;

public class GeneraInsertsEtiemba {

    EntradaMagatzem entradaMagatzem;

    public GeneraInsertsEtiemba(EntradaMagatzem entradaMagatzem) {
        this.entradaMagatzem = entradaMagatzem;
    }

    public PreparedStatementProviderBatch genera() {
        return conn -> new MultipleInsertStatement<EntradaMagatzemEmbalatge>(conn, "emb.etiemba")
                .add("etiqueta", EntradaMagatzemEmbalatge::etiqueta)
                .add("codielem", EntradaMagatzemEmbalatge::codiElement)
                .add("dataent", d -> entradaMagatzem.dataEntrada())
                .add("quant", EntradaMagatzemEmbalatge::quantitat)
                .add("usureg", d -> "ENTRADES")
                .add("datareg", d -> LocalDate.now())
                .build(entradaMagatzem.embalatges());
    }

}

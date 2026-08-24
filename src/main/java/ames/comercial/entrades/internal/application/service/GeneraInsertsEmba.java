package ames.comercial.entrades.internal.application.service;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.advantage.internal.auxiliar.statements.MultipleInsertStatement;
import ames.comercial.entrades.internal.domain.EntradaMagatzem;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemEmbalatge;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class GeneraInsertsEmba {

    EntradaMagatzem entradaMagatzem;

    public GeneraInsertsEmba(EntradaMagatzem entradaMagatzem) {
        this.entradaMagatzem = entradaMagatzem;
    }

    public PreparedStatementProviderBatch genera() {
        // Agrupació per codi element ja que només es fa un INSERT per cada codi element
        // i tots els camps son iguals, per tant s'agafa un element de cada llista de l'agrupació
        var listElements = entradaMagatzem.embalatges()
                .stream()
                .collect(Collectors.groupingBy(EntradaMagatzemEmbalatge::codiElement))
                .values()
                .stream()
                .map(list -> list.get(0))
                .toList();
        // Nomes cal afegir els elements que encara no estan a la taula
        var listElementsNoExisteixen = listElements.stream()
                .filter(e -> !isExisteixEmba(e.codiElement()))
                .toList();
        return insertsEmba(listElementsNoExisteixen);
    }

    private boolean isExisteixEmba(String codiElement) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement("""
                SELECT codielem
                FROM emb.elemba
                WHERE codielem = ?
            """);
            statement.setString(1, codiElement);
            return statement;
        };
        return new AdvantageDao().query(AdvantageDao.connectionMag, prep, ResultSet::next);
    }

    private PreparedStatementProviderBatch insertsEmba(List<EntradaMagatzemEmbalatge> emb) {
        return conn -> new MultipleInsertStatement<EntradaMagatzemEmbalatge>(conn, "emb.elemba")
                .add("codielem", EntradaMagatzemEmbalatge::codiElement)
                .add("descripcio", EntradaMagatzemEmbalatge::descripcio)
                .add("color", d -> "clWhite")
                .add("usureg", d -> "ENTRADES")
                .add("datareg", d -> LocalDate.now())
                .build(emb);
    }

}

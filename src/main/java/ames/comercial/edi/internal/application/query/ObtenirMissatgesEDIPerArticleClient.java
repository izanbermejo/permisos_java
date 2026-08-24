package ames.comercial.edi.internal.application.query;

import ames.comercial.edi.internal.domain.DadesMissatgeEDIPerArticle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

@Component
public class ObtenirMissatgesEDIPerArticleClient {

    @Autowired
    @Qualifier("jdbcAmes")
    private JdbcTemplate jdbcAmes;

    private static final String SQL = """
            SELECT DISTINCT c.codi, c.document, c.data, c.missatge_numero, c.enviament_numero, c.path_edi
            FROM edi.comanda c
            JOIN edi.linia l ON l.codi_comanda = c.codi
            WHERE c.codi_client_ames = ?
              AND l.codi_article = ?
              AND c.data >= ?
              AND c.data <= ?
            ORDER BY c.data DESC
            LIMIT 100
            """;

    public List<DadesMissatgeEDIPerArticle> executar(String cliCod, String artInt, LocalDate dataInici, LocalDate dataFi) {
        return jdbcAmes.query(
                SQL,
                (rs, rowNum) -> new DadesMissatgeEDIPerArticle(
                        rs.getLong("codi"),
                        rs.getString("document"),
                        rs.getTimestamp("data") != null ? rs.getTimestamp("data").toLocalDateTime() : null,
                        rs.getString("missatge_numero"),
                        rs.getString("enviament_numero"),
                        rs.getString("path_edi")
                ),
                cliCod,
                artInt,
                Timestamp.valueOf(dataInici.atStartOfDay()),
                Timestamp.valueOf(dataFi.plusDays(1).atStartOfDay())
        );
    }
}

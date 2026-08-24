package ames.comercial.comandes.internal.application.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ObtenirDetallComandesArticle {

    @Autowired @Qualifier("jdbcAmes")
    JdbcTemplate jdbcAmes;

    public List<ObtenirDetallComandesArticleResponse> executar(String article, String client, LocalDate dataInici, LocalDate dataFi, boolean mostrarEliminades) {
        var params = new MapSqlParameterSource();
        params.addValue("article", article)
                .addValue("client", client, Types.VARCHAR)
                .addValue("dataInicial", dataInici, Types.DATE)
                .addValue("dataFinal", dataFi, Types.DATE)
                .addValue("mostrarEliminades", mostrarEliminades, Types.BOOLEAN);
        return new NamedParameterJdbcTemplate(jdbcAmes).query("""
                    WITH comandes_client AS (
                         SELECT c.codi
                         FROM comandes.comanda c
                    ),
                    ultima_linia AS (
                         SELECT DISTINCT ON (lc.comanda, lc.numero) lc.comanda, lc.numero, lc.artint, lc.clicod, lc.tipus, lc.data_solicitada,
                             lc.data_prevista_sortida, lc.data_prevista_sortida_interna, lc.data_confirmada_fabrica, lc.quantitat,
                             lc.quantitat_servida, lc.quantitat_pendent, lc.preu, lc.divisa, lc.datareg, lc.datareg_local, lc.servida
                         FROM comandes.linia_comanda lc
                         JOIN comandes_client cc
                             ON cc.codi = lc.comanda
                         WHERE (lc.datareg_local <= :dataFinal)
                         and (
                            (:client IS NULL OR lc.clicod = :client)
                          )
                          and (
                            lc.artint = :article
                          )
                         ORDER BY lc.comanda, lc.numero, lc.datareg DESC
                    )
                    SELECT l.comanda, l.clicod, cc.nom, l.numero, c.informacio_client->>'identificador' AS identificador_client, l.tipus, ac.codi_fabrica,
                         ac.referencia, l.data_solicitada, l.data_prevista_sortida, l.data_prevista_sortida_interna, l.data_confirmada_fabrica,
                         l.quantitat, l.quantitat_servida, l.quantitat_pendent, l.preu, l.divisa, com.intern AS comIntern, com.client AS comClient
                    FROM ultima_linia l
                    JOIN comandes.comanda c
                         ON c.codi = l.comanda
                    LEFT JOIN comandes.linia_comanda_comentaris com
                         ON com.comanda = l.comanda
                         AND com.numero = l.numero
                    LEFT JOIN "cache".cache_article_client ac
                         ON ac.artint = l.artint
                         AND ac.clicod = l.clicod
                    LEFT JOIN "cache".cache_client cc
                         ON cc.clicod = l.clicod
                    WHERE
                         (
                             (l.datareg_local BETWEEN :dataInicial AND :dataFinal)
                             OR l.servida = false
                         )
                         AND (
                             :mostrarEliminades
                             OR l.quantitat > 0
                         )

                    ORDER BY
                         l.data_solicitada ASC, l.comanda ASC, l.numero ASC;
                """,
                params,
                this::mapAllDetallComandesArticle
        );
    }

    private ObtenirDetallComandesArticleResponse mapAllDetallComandesArticle(ResultSet rs, int rowNum) throws SQLException {
        return ObtenirDetallComandesArticleResponseImpl.builder()
                .comandaAMES(rs.getLong("comanda"))
                .numero(rs.getLong("numero"))
                .comandaClient(rs.getString("identificador_client"))
                .cliCod(rs.getString("clicod"))
                .nomClient(rs.getString("nom"))
                .tipus(rs.getString("tipus"))
                .codiFabrica(rs.getString("codi_fabrica"))
                .referencia(rs.getString("referencia"))
                .dataSolicitada(rs.getDate("data_solicitada") != null ? rs.getDate("data_solicitada").toLocalDate() : null)
                .dataPrevistaSortida(rs.getDate("data_prevista_sortida") != null ? rs.getDate("data_prevista_sortida").toLocalDate() : null)
                .dataPrevistaSortidaInterna(Optional.ofNullable(rs.getDate("data_prevista_sortida_interna")).map(Date::toLocalDate))
                .dataConfirmadaFabrica(Optional.ofNullable(rs.getDate("data_confirmada_fabrica")).map(Date::toLocalDate))
                .quantitat(rs.getLong("quantitat"))
                .quantitatServida(rs.getLong("quantitat_servida"))
                .quantitatPendent(rs.getLong("quantitat_pendent"))
                .preu(rs.getBigDecimal("preu"))
                .divisa(rs.getString("divisa"))
                .comIntern(Optional.ofNullable(rs.getString("comIntern")))
                .comClient(Optional.ofNullable(rs.getString("comClient")))
                .build();
    }

    @JsonDeserialize(builder = ObtenirDetallComandesArticleResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirDetallComandesArticleResponse {
        long comandaAMES();
        long numero();
        String comandaClient();
        String cliCod();
        String nomClient();
        String tipus();
        String codiFabrica();
        String referencia();
        LocalDate dataSolicitada();
        LocalDate dataPrevistaSortida();
        Optional<LocalDate> dataPrevistaSortidaInterna();
        Optional<LocalDate> dataConfirmadaFabrica();
        long quantitat();
        long quantitatServida();
        long quantitatPendent();
        BigDecimal preu();
        String divisa();
        Optional<String> comIntern();
        Optional<String> comClient();
    }
}

package ames.comercial.entrades.internal.application.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.ws.rs.QueryParam;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class BuscarEntradesComercial {

    @Autowired @Qualifier("jdbcAmes")
    JdbcTemplate jdbcAmes;

    public List<BuscarEntradesComercialResponse> executar(BuscarEntradesComercialRequest entradesComercial) {
        return executar(entradesComercial, Integer.MAX_VALUE);
    }

    public List<BuscarEntradesComercialResponse> executar(BuscarEntradesComercialRequest entradesComercial, int limit) {

        var params = new MapSqlParameterSource("idEntradaFabrica", entradesComercial.idEntradaFabrica.orElse(""))
                .addValue("article", entradesComercial.article.orElse(""))
                .addValue("client", entradesComercial.client.orElse(""))
                .addValue("magatzem", entradesComercial.magatzem.orElse(""))
                .addValue("dataInici", entradesComercial.dataInici, Types.DATE)
                .addValue("dataFi", entradesComercial.dataFi.plusDays(1), Types.DATE)
                .addValue("teError", entradesComercial.teError.orElse(null), Types.BOOLEAN)
                .addValue("limit", limit);

        return new NamedParameterJdbcTemplate(jdbcAmes).query(
                """
                        SELECT *
                        FROM entrades.entrada_comercial
                        WHERE
                            (
                                (:idEntradaFabrica is null or length(:idEntradaFabrica) = 0)
                                OR (id_entrada_fabrica::varchar like '%' || :idEntradaFabrica || '%')
                            )
                            AND
                            (
                                (:article is null or length(:article) = 0)
                                OR (article::varchar like '%' || :article || '%')
                            )
                            AND
                            (
                                (:client is null or length(:client) = 0)
                                OR (client::varchar like '%' || :client || '%')
                            )
                            AND
                            (
                                (:magatzem IS NULL or length(:magatzem) = 0)
                                OR
                                (:magatzem = magatzem)
                            )
                            AND -- Per data inici
                            (
                                (:dataInici IS NULL) OR data_alta >= :dataInici
                            )
                            AND -- Per data fi
                            (
                                (:dataFi IS NULL) OR data_alta <= :dataFi
                            )
                            AND
                            (
                                (:teError is null
                                OR
                                (:teError and error IS NOT NULL)
                                OR
                                (NOT :teError AND error IS NULL)
                            )
                            )
                        ORDER BY data_alta DESC
                        LIMIT :limit;
                """,
                params,
                this::mapAllEntradesComercial
        );
    }

    private BuscarEntradesComercialResponse mapAllEntradesComercial(ResultSet rs, int rowNum) throws SQLException {
        return ames.comercial.entrades.internal.application.query.BuscarEntradesComercialResponseImpl.builder()
                .id(rs.getString("id"))
                .idEntradaFabrica(rs.getString("id_entrada_fabrica"))
                .article(rs.getString("article"))
                .client(rs.getString("client"))
                .magatzem(rs.getString("magatzem"))
                .fabrica(rs.getString("fabrica"))
                .quantitat(rs.getInt("quantitat"))
                .quantitatCaixa(rs.getInt("quantitat_caixa"))
                .of(rs.getInt("of"))
                .dataEntrada(rs.getDate("data_entrada").toLocalDate())
                .pesPremsat(rs.getBigDecimal("pes_premsat"))
                .pesFinal(rs.getBigDecimal("pes_final"))
                .dataAlta(rs.getTimestamp("data_alta").toLocalDateTime())
                .dataProcessat(Optional.ofNullable(rs.getTimestamp("data_processat")).map(Timestamp::toLocalDateTime))
                .error(rs.getString("error") != null)
                .msgError(Optional.ofNullable(rs.getString("error")))
                .build();
    }

    public static class BuscarEntradesComercialRequest {
        @QueryParam("idEntradaFabrica") Optional<String> idEntradaFabrica;
        @QueryParam("article") Optional<String> article;
        @QueryParam("client") Optional<String> client;
        @QueryParam("magatzem") Optional<String> magatzem;
        @QueryParam("dataInici") LocalDate dataInici;
        @QueryParam("dataFi") LocalDate dataFi;
        @QueryParam("teError") Optional<Boolean> teError;
    }

    @JsonDeserialize(builder = BuscarEntradesComercialResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface BuscarEntradesComercialResponse {

        String id();
        String idEntradaFabrica();
        String article();
        String client();
        String magatzem();
        String fabrica();
        long quantitat();
        long quantitatCaixa();
        long of();
        LocalDate dataEntrada();
        BigDecimal pesPremsat();
        BigDecimal pesFinal();
        LocalDateTime dataAlta();
        Optional<LocalDateTime> dataProcessat();
        boolean error();
        Optional<String> msgError();

    }
}
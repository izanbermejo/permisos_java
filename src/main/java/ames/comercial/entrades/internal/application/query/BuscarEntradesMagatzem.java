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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class BuscarEntradesMagatzem {

    @Autowired @Qualifier("jdbcAmes")
    JdbcTemplate jdbcAmes;

    public List<BuscarEntradesMagatzemResponse> executar(BuscarEntradesMagatzemRequest entradesMagatzem) {

        var params = new MapSqlParameterSource("idEntradaFabrica", entradesMagatzem.idEntradaFabrica.orElse(null))
                .addValue("article", entradesMagatzem.article.orElse(null))
                .addValue("client", entradesMagatzem.client.orElse(null))
                .addValue("etiquetaCaixa", entradesMagatzem.etiquetaCaixa.orElse(null), Types.INTEGER)
                .addValue("lot", entradesMagatzem.lot.orElse(null))
                .addValue("magatzem", entradesMagatzem.magatzem.orElse(null))
                .addValue("dataInici", entradesMagatzem.dataInici, Types.DATE)
                .addValue("dataFi", entradesMagatzem.dataFi.plusDays(1), Types.DATE)
                .addValue("teError", entradesMagatzem.teError.orElse(null), Types.BOOLEAN);

        return new NamedParameterJdbcTemplate(jdbcAmes).query(
                """
                        SELECT em.id, em.id_entrada_fabrica, em.article, em.client, em.magatzem, em.quantitat, em.quantitat_caixa, em.etiqueta_caixa, em.etiqueta_palet, em.data_alta, em.error, MAX(emd.error) AS error_detall, COUNT(coalesce(emd.etiqueta_caixa, 1)) AS quantitat_caixes
                        FROM entrades.entrada_magatzem em
                        LEFT JOIN entrades.entrada_magatzem_detall emd ON em.id = emd.id
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
                                (:etiquetaCaixa IS NULL)
                                OR
                                (:etiquetaCaixa = em.etiqueta_caixa)
                                OR
                                (:etiquetaCaixa = emd.etiqueta_caixa)
                            )
                            AND
                            (
                                (:lot IS NULL or length(:lot) = 0)
                                OR
                                (:lot = em.lot)
                                OR
                                (:lot = emd.lot)
                            )
                            AND
                            (
                                (:magatzem IS NULL or length(:magatzem) = 0)
                                OR
                                (:magatzem = em.magatzem)
                            )
                        GROUP BY em.id, em.id_entrada_fabrica, em.article, em.client, em.magatzem, em.quantitat, em.quantitat_caixa, em.etiqueta_caixa, em.etiqueta_palet, em.data_alta, em.error
                        HAVING
                            (
                                (:teError is null)
                                OR
                                (:teError and (em.error IS NOT NULL OR max(emd.error) IS NOT NULL))
                                OR
                                (NOT :teError AND em.error IS NULL AND max(emd.error) IS NULL)
                            )
                        ORDER BY data_alta DESC
                        LIMIT 100;
                """,
                params,
                this::mapAllEntradesMagatzem
        );
    }

    private BuscarEntradesMagatzemResponse mapAllEntradesMagatzem(ResultSet rs, int rowNum) throws SQLException {
        return BuscarEntradesMagatzemResponseImpl.builder()
                .id(rs.getString("id"))
                .idEntradaFabrica(rs.getString("id_entrada_fabrica"))
                .article(rs.getString("article"))
                .client(rs.getString("client"))
                .magatzem(rs.getString("magatzem"))
                .quantitat(rs.getInt("quantitat"))
                .quantitatCaixa(rs.getInt("quantitat_caixa"))
                .etiquetaCaixa(rs.getInt("etiqueta_caixa"))
                .etiquetaPalet(rs.getInt("etiqueta_palet"))
                .error(rs.getString("error") != null)
                .errorDetall(rs.getString("error_detall") != null)
                .msgError(Optional.ofNullable(rs.getString("error")))
                .msgErrorDetall(Optional.ofNullable(rs.getString("error_detall")))
                .quantitatCaixes(rs.getInt("quantitat_caixes"))
                .dataAlta(rs.getTimestamp("data_alta").toLocalDateTime())
                .build();
    }

    public static class BuscarEntradesMagatzemRequest {
        @QueryParam("idEntradaFabrica") Optional<String> idEntradaFabrica;
        @QueryParam("article") Optional<String> article;
        @QueryParam("client") Optional<String> client;
        @QueryParam("etiquetaCaixa") Optional<Integer> etiquetaCaixa;
        @QueryParam("lot") Optional<String> lot;
        @QueryParam("magatzem") Optional<String> magatzem;
        @QueryParam("dataInici") LocalDate dataInici;
        @QueryParam("dataFi") LocalDate dataFi;
        @QueryParam("teError") Optional<Boolean> teError;
    }

    @JsonDeserialize(builder = BuscarEntradesMagatzemResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface BuscarEntradesMagatzemResponse {

        String id();
        String idEntradaFabrica();
        String article();
        String client();
        String magatzem();
        long quantitat();
        long quantitatCaixa();
        long etiquetaCaixa();
        long etiquetaPalet();
        boolean error();
        boolean errorDetall();
        Optional<String> msgError();
        Optional<String> msgErrorDetall();
        long quantitatCaixes();
        LocalDateTime dataAlta();
    }
}
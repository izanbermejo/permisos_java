package ames.comercial.entrades.internal.application.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirEntradaMagatzemByIdFabrica {

    @Autowired JdbcTemplate jdbcAmes;

    public List<ObtenirEntradaMagatzemByIdResponse> executar(String idEntradaFabrica) {
        var sql = """
                SELECT em.id, em.id_entrada_fabrica, em.article, em.client, em.quantitat, em.quantitat_caixa, em.etiqueta_caixa, em.etiqueta_palet, em.data_entrada, em.error, emd.error as error_detall, COUNT(emd.etiqueta_caixa) AS quantitat_caixes
                FROM entrades.entrada_magatzem em
                LEFT JOIN entrades.entrada_magatzem_detall emd ON em.id = emd.id
                WHERE id_entrada_fabrica = ?
                GROUP BY em.id, em.id_entrada_fabrica, em.article, em.client, em.quantitat, em.quantitat_caixa, em.etiqueta_caixa, em.etiqueta_palet, em.data_entrada, em.error, emd.error
                ORDER BY data_entrada DESC;
                """;
        return jdbcAmes.query(sql, this::map, idEntradaFabrica);
    }

    private ObtenirEntradaMagatzemByIdResponse map(ResultSet rs, int rowNum) throws SQLException {
        return ObtenirEntradaMagatzemByIdResponseImpl.builder()
                .id(rs.getString("id"))
                .idEntradaFabrica(rs.getString("id_entrada_fabrica"))
                .article(rs.getString("article"))
                .client(rs.getString("client"))
                .quantitat(rs.getLong("quantitat"))
                .quantitatCaixa(rs.getLong("quantitat_caixa"))
                .etiquetaCaixa(rs.getLong("etiqueta_caixa"))
                .etiquetaPalet(rs.getLong("etiqueta_palet"))
                .error(rs.getString("error") != null)
                .errorDetall(rs.getString("error_detall") != null)
                .msgError(Optional.ofNullable(rs.getString("error")))
                .msgErrorDetall(Optional.ofNullable(rs.getString("error_detall")))
                .quantitatCaixes(rs.getLong("quantitat_caixes"))
                .build();
    }

    @JsonDeserialize(builder = ObtenirEntradaMagatzemByIdResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirEntradaMagatzemByIdResponse {
        String id();
        String idEntradaFabrica();
        String article();
        String client();
        long quantitat();
        long quantitatCaixa();
        long etiquetaCaixa();
        long etiquetaPalet();
        boolean error();
        boolean errorDetall();
        Optional<String> msgError();
        Optional<String> msgErrorDetall();
        long quantitatCaixes();
    }

}


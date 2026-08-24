package ames.comercial.albarans.internal.application.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Obté les peces amb stock en un magatzem plataforma per poder-hi fer un consum. L'stock és el de la
 * fitxa d'inventari ({@code inventari.fitxa}) del magatzem, amb JOIN a la caché d'article-client
 * ({@code cache.cache_article_client}) per obtenir el codi de fàbrica (aclfab), la referència i la
 * denominació en una sola consulta.
 */
@Service
public class ObtenirStockPlataforma {

    @Autowired NamedParameterJdbcTemplate jdbcAmes;

    public List<PecaStockPlataformaResponse> executar(String magatzemPlataforma) {
        var params = new MapSqlParameterSource().addValue("magatzem", magatzemPlataforma);
        String sql = """
                SELECT f.artint, f.clicod, f.empresa, f.stock,
                       c.codi_fabrica, c.referencia, c.denominacio
                FROM inventari.fitxa f
                LEFT JOIN cache.cache_article_client c
                    ON c.artint = f.artint AND c.clicod = f.clicod
                WHERE f.magatzem = :magatzem
                    AND f.stock <> 0
                ORDER BY f.artint, f.clicod
                """;
        return jdbcAmes.query(sql, params, (rs, rowNum) -> PecaStockPlataformaResponseImpl.builder()
                .empresa(rs.getString("empresa"))
                .artint(rs.getString("artint"))
                .clicod(rs.getString("clicod"))
                .aclfab(Optional.ofNullable(rs.getString("codi_fabrica")).orElse(""))
                .referencia(Optional.ofNullable(rs.getString("referencia")).orElse(""))
                .denominacio(Optional.ofNullable(rs.getString("denominacio")).orElse(""))
                .stock(rs.getLong("stock"))
                .build());
    }

    @JsonDeserialize(builder = PecaStockPlataformaResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface PecaStockPlataformaResponse {
        String empresa();
        String artint();
        String clicod();
        /** Codi de fàbrica de l'article-client (aclfab). El codi de 13 = aclfab + clicod. */
        String aclfab();
        String referencia();
        String denominacio();
        /** Stock físic actual a la fitxa d'inventari del magatzem plataforma */
        long stock();
    }

}

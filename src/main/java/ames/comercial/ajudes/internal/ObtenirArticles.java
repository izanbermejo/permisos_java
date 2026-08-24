package ames.comercial.ajudes.internal;

import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ObtenirArticles {

	@Autowired NamedParameterJdbcTemplate jdbcTemplate;

	public List<QueryArticleResponse> query(String filtre, String client, TipusArticleClient tipus) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("client", client);
		params.addValue("filtreIsBlank", filtre.isBlank());
		params.addValue("filtre", "%" + filtre + "%");
		params.addValue("tipus", tipus.name());
		// En el cas d'IBINSA no cal mirar el tipus ja que s'han de mostrar tant els especials com els propis
		// d'Ibinsa
		params.addValue("tipusIsIbinsa", TipusArticleClient.IBINSA.equals(tipus));

		var query = """
				SELECT ac.artint, ac.clicod, ac.codi_fabrica, ac.referencia, ac.denominacio, ac.fabrica_codi,
				COALESCE(f.stock,0) AS stock,
				ac.preu, ac.divisa,
				ac.unitats_embalatge, ac.caixes_palet, ac.tipus
				FROM cache.cache_article_client ac
				LEFT JOIN (
					SELECT artint, clicod, SUM(stock) stock
					FROM inventari.fitxa
					GROUP BY artint, clicod
				) f ON f.artint = ac.artint AND f.clicod = ac.clicod
				WHERE (
					ac.clicod = :client -- Peces del client
					AND (:filtreIsBlank OR ac.referencia ILIKE :filtre) -- Filtre de la referència
					AND ac.flag = 'A' -- Només articles actius
					AND (:tipusIsIbinsa OR ac.tipus = :tipus) -- Filtre de tipus d'article
				)
				ORDER BY ac.referencia;
				""";

		return jdbcTemplate.query(query, params, (rs, rowNum) -> QueryArticleResponseImpl.builder()
				.artint(rs.getString("artint"))
				.clicod(rs.getString("clicod"))
				.codi(rs.getString("codi_fabrica"))
				.tipus(TipusArticleClient.getByTipus(rs.getString("tipus")))
				.referencia(rs.getString("referencia"))
				.denominacio(rs.getString("denominacio"))
				.fabrica(rs.getString("fabrica_codi"))
				.stock(rs.getLong("stock"))
				.preu(rs.getBigDecimal("preu") != null ? rs.getBigDecimal("preu") : BigDecimal.ZERO)
				.divisa(rs.getString("divisa"))
				.unitatsEmbalatge(rs.getLong("unitats_embalatge"))
				.caixesPalet(rs.getLong("caixes_palet"))
				.build());

	}

	@JsonDeserialize(builder = QueryArticleResponseImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	public interface QueryArticleResponse {

		String artint();
		String clicod();
		String codi();
		TipusArticleClient tipus();
		String referencia();
		String denominacio();
		String fabrica();
		long stock();
		BigDecimal preu();
		String divisa();
		long unitatsEmbalatge();
		long caixesPalet();

	}
	
}

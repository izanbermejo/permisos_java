package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirArticlesClientNormalitzatsByReferencia;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ObtenirArticlesClientNormalitzatsByReferencia implements IObtenirArticlesClientNormalitzatsByReferencia {

	@Autowired JdbcTemplate jdbcTemplate;

	@Override
	public Map<String, ArticleClientUnitatsEmbalatge> query(Set<String> referencies) {
		String sql = String.format("""
				WITH stock_article AS (
				    SELECT artint, clicod, SUM(stock) AS stock
				    FROM inventari.fitxa
				    GROUP BY artint, clicod
				)
				SELECT
				    ac.artint,
				    ac.clicod,
				    ac.referencia,
				    ac.unitats_embalatge,
				    sa.stock
				FROM cache.cache_article_client ac
				LEFT JOIN stock_article sa
				       ON sa.artint = ac.artint
				      AND sa.clicod = ac.clicod
				WHERE ac.clicod = '000000'
					AND ac.referencia IN (%s)
				ORDER BY ac.artint DESC;
				""", referencies.stream().map(v -> "?").collect(Collectors.joining(", ")));

		Object[] params = referencies.toArray();
		return jdbcTemplate.query(sql, ps -> {
			int index = 1;
			for (Object param : params) {
				ps.setObject(index++, param);
			}
		}, rs -> {
			Map<String, ArticleClientUnitatsEmbalatge> resultat = new HashMap<>();
			while (rs.next()) {
				// S'ordena per artint així s'omple el map amb les referències mes noves primer (codi fàbrica acaba en B)
				// Quan arriba a les referenciès mes antigues només s'afegeixen en cas que tinguin stock
				var articleClient = resultat.get(rs.getString("referencia"));
				if (articleClient != null) {
					// Ja existeix en el map aquesta referència
					if (rs.getLong("stock") > 0)
						resultat.put(rs.getString("referencia"), mapArticleClientUnitatsEmbalatge(rs));
				} else {
					resultat.put(rs.getString("referencia"), mapArticleClientUnitatsEmbalatge(rs));
				}
			}
			return resultat;
		});
	}

	private ArticleClientUnitatsEmbalatge mapArticleClientUnitatsEmbalatge(ResultSet rs) throws SQLException {
		return new ArticleClientUnitatsEmbalatge(
				KeyArticleClient.of(rs.getString("artint"),rs.getString("clicod")),
				rs.getLong("unitats_embalatge")
		);
	}

	public record ArticleClientUnitatsEmbalatge(KeyArticleClient articleClient, long unitatsEmbalatge) {}

}

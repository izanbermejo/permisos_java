package ames.comercial.ajudes.internal;

import ames.comercial.advantage.internal.response.QueryArticleNormalitzatResponse;
import ames.comercial.advantage.internal.response.QueryArticleNormalitzatResponseImpl;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.TipusArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObtenirArticlesNormalitzats {

	@Autowired NamedParameterJdbcTemplate jdbcTemplate;

	public List<QueryArticleNormalitzatResponse> query(String filtre, Empresa empresa, TipusArticleClient tipus) {
		// Empresa de refèrencia per l'stock local
		var empresaStock = Empresa.referenciaStock(empresa);
		// Construcció dels paràmetres de la consulta

		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("empresa", empresaStock.clau());
		params.addValue("magatzem", empresaStock.magatzem());
		params.addValue("tipus", tipus.name());
		params.addValue("refIsBlank", filtre.isBlank());
		params.addValue("referencia", "%" + filtre + "%");

		// Execució de la query i mapeig del resultat a la resposta
		return jdbcTemplate.query("""
						SELECT ac.artint, ac.clicod, ac.codi_fabrica, ac.referencia, ac.denominacio, ac.fabrica_codi,
							SUM(f.stock) AS "stockTotal",
							SUM(f.stock_reservat) AS "stockTotalReservat",
							SUM(f.stock) FILTER (WHERE f.empresa = :empresa AND f.magatzem = :magatzem) AS "stockLocal",
							SUM(f.stock_reservat) FILTER (WHERE f.empresa = :empresa AND f.magatzem = :magatzem) AS "stockLocalReservat",
							ac.unitats_embalatge, ac.bosses_caixa, ac.caixes_palet, ac.tipus
						FROM cache.cache_article_client ac
						LEFT JOIN inventari.fitxa f ON f.artint = ac.artint AND f.clicod = ac.clicod
						WHERE
							(ac.clicod = '000000') -- Normalitzats
							AND tipus = :tipus	-- Tipus normalitzats
							AND	(:refIsBlank OR ac.referencia ILIKE :referencia) -- Filtre de la referència
							AND (ac.flag = 'A')	-- Només articles actius
						GROUP BY ac.artint, ac.clicod, ac.codi_fabrica, ac.referencia, ac.denominacio,
							ac.fabrica_codi, ac.unitats_embalatge, ac.bosses_caixa, ac.caixes_palet, ac.tipus
						ORDER BY ac.referencia;
						""", params,
				(rs, rowNum) -> QueryArticleNormalitzatResponseImpl.builder()
						.artint(rs.getString("artint"))
						.clicod(rs.getString("clicod"))
						.codi(rs.getString("codi_fabrica"))
						.referencia(rs.getString("referencia"))
						.denominacio(rs.getString("denominacio"))
						.fabrica(rs.getString("fabrica_codi"))
						.stock(rs.getLong("stockTotal"))
						.reserva(rs.getLong("stockTotalReservat"))
						.stockLocal(rs.getLong("stockLocal"))
						.reservaLocal(rs.getLong("stockLocalReservat"))
						.unitatsEmbalatge(rs.getLong("unitats_embalatge"))
						.bossesCaixa(rs.getLong("bosses_caixa"))
						.caixesPalet(rs.getLong("caixes_palet"))
						.build());
	}

}

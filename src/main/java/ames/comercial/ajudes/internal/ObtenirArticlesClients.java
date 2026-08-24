package ames.comercial.ajudes.internal;

import ames.comercial.advantage.internal.response.ObtenirArticlesClientsResponse;
import ames.comercial.advantage.internal.response.ObtenirArticlesClientsResponseImpl;
import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ObtenirArticlesClients {

	@Autowired NamedParameterJdbcTemplate jdbcTemplate;

	public List<ObtenirArticlesClientsResponse> query(ObtenirArticlesClientsRequest request) {
		MapSqlParameterSource params = new MapSqlParameterSource();
		params.addValue("artintIsBlank", request.artint().map(String::isBlank).orElse(true));
		params.addValue("artint", request.artint().orElse(""));
		params.addValue("clicodIsBlank", request.clicod().map(String::isBlank).orElse(true));
		params.addValue("clicod", request.clicod().orElse(""));
		params.addValue("filtreIsBlank", request.filtre().isBlank());
		params.addValue("filtre", "%" + request.filtre() + "%");
		params.addValue("incloureInactius", request.incloureInactius());

		// Execució de la query i mapeig del resultat a la resposta
		return jdbcTemplate.query(queryOrder("""
						SELECT ac.referencia, ac.codi_fabrica, ac.denominacio, ac.artint, ac.flag, ac.clicod, ac.empresa, ac.fabrica_codi,
						ac.nivell_tecnic, ac.pesa_seguretat, ac.forma_enviament,
						ac.preu, ac.divisa,
						COALESCE(f.stock,0) AS stock,
						ac.bloquejat, ac.usuari_bloqueig,
						ac.unitats_embalatge, ac.bosses_caixa, ac.caixes_palet, ac.tipus,
						ac.partida_arant_codi, ac.partida_arant_desc, ac.partida_arant_partida,
						cli.nom, cli.alias, cli.zonatra_codi, cli.empresa AS "empresaClient", cli.codi_proveidor
						FROM CACHE.cache_article_client ac
						LEFT JOIN "cache".cache_client cli ON ac.clicod = cli.clicod
						LEFT JOIN (
						    SELECT artint, clicod, SUM(stock) stock
						    FROM inventari.fitxa
						    GROUP BY artint, clicod
						) f ON f.artint = ac.artint AND f.clicod = ac.clicod
						WHERE 1=1
							AND (:artintIsBlank OR ac.artint = :artint)
							AND (:clicodIsBlank OR ac.clicod = :clicod)
							AND (:filtreIsBlank OR (
								ac.codi_fabrica ILIKE :filtre
								OR
								ac.denominacio ILIKE :filtre
								OR
								ac.referencia ILIKE :filtre
								OR
								cli.nom ILIKE :filtre
								OR
								cli.alias ILIKE :filtre
								OR
								cli.clicod ILIKE :filtre
								OR
								cli.codi_proveidor ILIKE :filtre
								OR
								CONCAT(ac.codi_fabrica, cli.clicod) ILIKE :filtre
								)
							)
							AND (:incloureInactius OR ac.flag = 'A')
						""", request.colOrder(), request.asc()) + " LIMIT 100 ",
				params,
				(resultSet, rowNum) -> ObtenirArticlesClientsResponseImpl.builder()
						.artInt(resultSet.getString("artint"))
						.flag(resultSet.getString("flag"))
						.referencia(resultSet.getString("referencia"))
						.article(resultSet.getString("codi_fabrica"))
						.codiClient(resultSet.getString("clicod"))
						.nomClient(resultSet.getString("nom"))
						.aliasClient(resultSet.getString("alias"))
						.formaEnviament(resultSet.getString("forma_enviament"))
						.codiProveidor(resultSet.getString("codi_proveidor"))
						.codiEmpresa(resultSet.getString("empresa"))
						.denominacio(resultSet.getString("denominacio"))
						.nivellTecnic(resultSet.getString("nivell_tecnic"))
						.pesaSeguretat(resultSet.getBoolean("pesa_seguretat"))
						.codiFabrica(resultSet.getString("fabrica_codi"))
						.preu(resultSet.getBigDecimal("preu") != null ? resultSet.getBigDecimal("preu") : BigDecimal.ZERO)
						.divisa(resultSet.getString("divisa"))
						.stock(resultSet.getInt("stock"))
						.bloquejatStock(resultSet.getBoolean("bloquejat"))
						.usuariBloqueigStock(resultSet.getString("usuari_bloqueig"))
						.unitatsEmbalatge(resultSet.getInt("unitats_embalatge"))
						.bossesCaixa(resultSet.getInt("bosses_caixa"))
						.caixesPalet(resultSet.getInt("caixes_palet"))
						.tipus(TipusArticleClient.getByTipus(resultSet.getString("tipus")))
						.partidaArantzelariaCodiAmes(Optional.ofNullable(resultSet.getString("partida_arant_codi")))
						.partidaArantzelariaDescripcio(Optional.ofNullable(resultSet.getString("partida_arant_desc")))
						.partidaArantzelariaCodi(Optional.ofNullable(resultSet.getString("partida_arant_partida")))
						.build());
	}

	private String queryOrder (String query, String colName, boolean asc) {
		if (colName == null || colName.isBlank())
			return query;
		return query + " ORDER BY " + colName + (asc ? " ASC" : " DESC");
	}

	@JsonDeserialize(builder = ObtenirArticlesClientsRequestImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	public interface ObtenirArticlesClientsRequest {
		Optional<String> artint();
		Optional<String> clicod();
		String filtre();
		boolean incloureInactius();
		String colOrder();
		boolean asc();
	}

}

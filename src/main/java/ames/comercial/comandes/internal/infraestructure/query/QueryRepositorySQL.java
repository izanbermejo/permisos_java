package ames.comercial.comandes.internal.infraestructure.query;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.infraestructure.query.extractor.AlbaraServitPerLiniaExtractor;
import ames.comercial.comandes.internal.infraestructure.query.extractor.ItemHistoriaLiniaComandaMapper;
import ames.comercial.comandes.internal.infraestructure.query.extractor.ItemLiniaComandaExtractor;
import ames.comercial.comandes.response.AlbaraServitLiniaComanda;
import ames.comercial.comandes.response.ItemHistoriaLiniaComanda;
import ames.comercial.comandes.response.ItemLiniaComanda;
import ames.comercial.server.Json;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository("EDIQueryRepositorySQL")
public class QueryRepositorySQL implements QueryRepository {
	
	private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
	private @Autowired ObjectMapper jsonMapper;
	private Json json;
	
	@PostConstruct
	public void init () {
		json = new Json(jsonMapper);
	}

	@Override
	public List<ItemLiniaComanda> searchLiniesPeriode(String artint, String clicod, LocalDate dataInicial, LocalDate dataFinal) {
		var sql = """
				SELECT l.*, c.*, com.intern as "comIntern", com.client as "comClient"
				FROM comandes.linia_comanda l
				RIGHT JOIN (
					SELECT lc.comanda, lc.numero, MAX(lc.datareg) AS datareg
					FROM comandes.linia_comanda lc
					WHERE datareg_local <= ? AND artint = ? and clicod = ?
					GROUP BY lc.comanda, lc.numero) s ON l.comanda = s.comanda AND l.numero = s.numero AND l.datareg = s.datareg
				LEFT JOIN comandes.comanda c ON c.codi = l.comanda
				LEFT JOIN comandes.linia_comanda_comentaris com ON l.comanda = com.comanda AND l.numero = com.numero
				WHERE (l.datareg_local BETWEEN ? AND ? OR l.servida = false)
					AND quantitat > 0
				ORDER BY data_solicitada ASC, l.comanda ASC, l.numero ASC;
				""";
		return jdbcAmes.query(sql, new ItemLiniaComandaExtractor(json), dataFinal, artint, clicod, dataInicial, dataFinal);
	}
	
	@Override
	public Map<KeyLiniaComanda, List<AlbaraServitLiniaComanda>> searchAlbaransServitsPerLinia(KeyArticleClient articleClient) {
		var sql = """
				WITH moviments AS (
					SELECT
						m.sortida_comanda_codi   AS comanda,
						m.sortida_comanda_numero AS numero,
						m.linia_albara_empresa   AS empresa,
						m.linia_albara_numero    AS albara,
						SUM(m.quantitat)         AS quantitat
					FROM inventari.moviment m
					WHERE m.tipus = 'SORTIDA'
						AND m.artint = ? AND m.clicod = ?
						AND m.sortida_comanda_codi IS NOT NULL
					GROUP BY m.sortida_comanda_codi, m.sortida_comanda_numero,
						m.linia_albara_empresa, m.linia_albara_numero)
				SELECT
					mv.comanda               AS comanda,
					mv.numero                AS numero,
					mv.albara                AS albara,
					a.numero_albara_especial AS albara_especial,
					a."data"                 AS data_albara,
					a.informacio_enviament   AS informacio_enviament,
					a.informacio_magatzem    AS informacio_magatzem,
					mv.quantitat             AS quantitat,
					(SELECT STRING_AGG(DISTINCT f.codi_factura, ', ')
						FROM albarans.facturacio f
						WHERE f.empresa = mv.empresa AND f.codi_albara = mv.albara) AS factures
				FROM moviments mv
				LEFT JOIN albarans.albara a
					ON a.empresa = mv.empresa AND a.codi = mv.albara
				ORDER BY a."data" DESC;
				""";
		return jdbcAmes.query(sql, new AlbaraServitPerLiniaExtractor(json), articleClient.artint(), articleClient.clicod());
	}

	@Override
	public List<ItemHistoriaLiniaComanda> searchHistoriaLiniaComanda(long comanda, long numero) {
		var sql = """
				SELECT * FROM comandes.linia_comanda WHERE comanda = ? AND numero = ? ORDER BY datareg DESC
				""";
		return jdbcAmes.query(sql, new ItemHistoriaLiniaComandaMapper(), comanda, numero);
	}

}

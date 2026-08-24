package ames.comercial.comandes.internal.infraestructure.liniacomanda;

import ames.comercial.comandes.ComandesException.LiniaComandaChanged;
import ames.comercial.comandes.internal.domain.linia.InformacioReserva;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.comandes.internal.infraestructure.liniacomanda.mapper.LiniaComandaMapper;
import ames.comercial.server.ETag;
import ames.comercial.server.Json;
import ames.comercial.server.RequestThread;
import ames.comercial.server.RequestThread.Entity;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Pair;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
public class LiniaComandaRepositorySQL implements LiniaComandaRepository {
	
	private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
	private @Autowired ObjectMapper jsonMapper;
	private Json json;
	
	@PostConstruct
	public void init () {
		json = new Json(jsonMapper);
	}	
	
	@Override
	public long nextNumero(long comanda) {
		return jdbcAmes.queryForObject("SELECT coalesce(MAX(numero),0)+1 FROM comandes.linia_comanda WHERE comanda = ?", Long.class, comanda);
	}

	@Override
	public void save(LiniaComanda l) {
		checkEtag(l.comanda(), l.numero());
		updateActual(l.comanda(), l.numero());
		
		new SimpleJdbcInsert(jdbcAmes)
			.withSchemaName("comandes")	
			.withTableName("linia_comanda")
			.usingGeneratedKeyColumns("datareg")
			.execute(params(l));
	}
	
	@Override
	public void save(List<LiniaComanda> l) {
		l.forEach(this::save);
	}	

	private HashMap<String, Object> params(LiniaComanda l) {
		var params = new HashMap<String, Object>();
		var newEtag = ETag.generate();
		params.put("comanda", l.comanda());
		params.put("numero", l.numero());
		params.put("artint", l.articleClient().artint());
		params.put("clicod", l.articleClient().clicod());
		params.put("referencia", l.referencia());
		params.put("tipus_article_client", l.tipusArticleClient());
		params.put("tipus", l.tipus());
		params.put("quantitat", l.quantitat());
		params.put("preu", l.preu().valor());
		params.put("divisa", l.preu().divisa());
		params.put("preu_fixat", l.isPreuFixat());
		params.put("data_creacio", l.dataCreacio());
		params.put("data_solicitada", l.dataSolicitada());
		params.put("data_prevista_sortida", l.dataPrevistaSortida());
		params.put("data_prevista_sortida_interna", l.dataPrevistaSortidaInterna().orElse(null));
		params.put("data_confirmada_fabrica", l.dataConfirmadaFabrica().orElse(null));
		params.put("quantitat_servida", l.quantitatServida());
		params.put("quantitat_pendent", l.quantitatPendent());
		params.put("quantitat_reservada", l.reserva().map(InformacioReserva::quantitat).orElse(0L));
		params.put("estat_reserva", l.reserva().map(InformacioReserva::estat).orElse(null));
		params.put("servida", l.servida());
		params.put("dades_calcul", l.dadesCalcul().isEmpty() ? null : json.serialize(l.dadesCalcul()));
		params.put("comanda_blanca", (l.comandaBlanca().orElse(0L) == 0L) ? null : l.comandaBlanca().get());	// La CB es posa NULL si és 0
		params.put("actual", true);
		params.put("versio", newEtag.versio());
		params.put("usuari", newEtag.usuari());
		params.put("datareg_local", RequestThread.dateLocal());
		return params;
	}
	
	private void updateActual(long comanda, long numero) {
		jdbcAmes.update("UPDATE comandes.linia_comanda SET actual = false WHERE comanda = ? AND numero = ?", comanda, numero);
	}

	@Override
	public Optional<LiniaComanda> find(long comanda, long numero) {
		try {
			return Optional.ofNullable(jdbcAmes.queryForObject("SELECT * FROM comandes.linia_comanda WHERE comanda = ? AND numero = ? AND actual = true",
					new LiniaComandaMapper(json), comanda, numero));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<LiniaComanda> find(KeyLiniaComanda clauLiniaComanda) {
		return find(clauLiniaComanda.comanda(), clauLiniaComanda.numero());
	}

	@Override
	public List<LiniaComanda> find(List<KeyLiniaComanda> ids) {
		if (ids.isEmpty()) return List.of();

		// PostgreSQL permet passar claus compostes en clàusules IN mitjançant la sintaxi (col1, col2) IN ((val1, val2), (val3, val4), ...)
		String placeholders = ids.stream()
				.map(id -> "(?, ?)")
				.collect(Collectors.joining(", "));
		String sql = """
        SELECT *
        FROM comandes.linia_comanda
        WHERE actual = true
          AND (comanda, numero) IN ( %s )
        """.formatted(placeholders);
		Object[] params = ids.stream()
				.flatMap(id -> Stream.of(id.comanda(), id.numero()))
				.toArray();

		return jdbcAmes.query(sql, params, new LiniaComandaMapper(json));
	}

	@Override
	public Optional<LiniaComanda> findStockSeguretat(KeyArticleClient articleClient) {
		try {
			return Optional.ofNullable(jdbcAmes.queryForObject("""
				SELECT * FROM comandes.linia_comanda lc
				WHERE lc.artint = ?
					AND lc.clicod = ?
					AND lc.actual
					AND (lc.tipus = 'STOCK_SEG_AMES' OR lc.tipus = 'STOCK_SEG_CLIENT')
				""",
					new LiniaComandaMapper(json), articleClient.artint(), articleClient.clicod()));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public List<LiniaComanda> findByComanda(long comanda) {
		return findByComanda(comanda, false);
	}

	@Override
	public List<LiniaComanda> findByComanda(long comanda, boolean includeZero) {
		return jdbcAmes.query("""
				SELECT * FROM comandes.linia_comanda
				 WHERE comanda = ? AND actual = true
				 AND (? OR quantitat > 0)
				 ORDER BY data_solicitada ASC, numero
				""",
				new LiniaComandaMapper(json), comanda, includeZero);
	}

	@Override
	public List<LiniaComanda> findByArticlePendent(KeyArticleClient articleClient) {
		return findByArticlePendent(articleClient, null);
	}

	@Override
	public List<LiniaComanda> findByArticlePendent(KeyArticleClient articleClient, Empresa empresa) {
		return findByArticlePendent(articleClient, empresa, false);
	}

	@Override
	public List<LiniaComanda> findByArticlePendent(KeyArticleClient articleClient, Empresa empresa, boolean includeZero) {
		String sqlQuery = """
				SELECT * FROM comandes.linia_comanda lc
				LEFT JOIN comandes.comanda c ON lc.comanda = c.codi
				WHERE lc.artint = ?
					AND lc.clicod = ?
					AND (? OR c.empresa = ?)
					AND NOT lc.servida
					AND lc.actual
					AND (? OR quantitat > 0);
				""";
		// El paràmetre pot ser null
		var empresaParam = empresa != null ? empresa.clau() : null;
		return jdbcAmes.query(sqlQuery,
				new LiniaComandaMapper(json),
				articleClient.artint(),
				articleClient.clicod(),
				ObjectUtils.isEmpty(empresaParam),
				empresaParam,
				includeZero);
	}

	@Override
	public List<Pair<LocalDateTime, LiniaComanda>> findByMomentPosterior(LocalDateTime moment) {
		String sqlQuery = """
				SELECT * FROM comandes.linia_comanda lc
				LEFT JOIN comandes.comanda c ON lc.comanda = c.codi
				WHERE datareg > ?
				ORDER BY datareg ASC;
				""";
		return jdbcAmes.query(sqlQuery, (rs, rowNum) -> {
			LocalDateTime datareg = rs.getObject("datareg", LocalDateTime.class);
			LiniaComanda linia = new LiniaComandaMapper(json).mapRow(rs, rowNum);
			return new Pair<>(datareg, linia);
		}, moment);
	}

	@Override
	public Optional<Pair<LocalDateTime, LiniaComanda>> findVersioAnterior(KeyLiniaComanda clauLinia, LocalDateTime moment) {
		String sqlQuery = """
			SELECT * FROM comandes.linia_comanda lc
			LEFT JOIN comandes.comanda c ON lc.comanda = c.codi
			WHERE lc.comanda = ?
			  AND lc.numero = ?
			  AND lc.datareg < ?
			ORDER BY lc.datareg DESC
			LIMIT 1
			""";
		List<Pair<LocalDateTime, LiniaComanda>> result = jdbcAmes.query(sqlQuery, (rs, rowNum) -> {
			LocalDateTime datareg = rs.getObject("datareg", LocalDateTime.class);
			LiniaComanda linia = new LiniaComandaMapper(json).mapRow(rs, rowNum);
			return new Pair<>(datareg, linia);
		}, clauLinia.comanda(), clauLinia.numero(), moment);

		return result.stream().findFirst();
	}

	@Override
	public long quantitatPendent(long comanda) {
		return jdbcAmes.queryForObject("SELECT coalesce(sum(quantitat_pendent),0) FROM comandes.linia_comanda WHERE comanda = ? AND actual = true", 
				Long.class, comanda);
	}

	@Override
	public Optional<LocalDate> dataUltimaComanda(KeyArticleClient articleClient) {
		return Optional.ofNullable(jdbcAmes.queryForObject("""
			SELECT MAX(data_solicitada)
			FROM comandes.linia_comanda lc
			WHERE actual
				AND quantitat > 0
				AND artint = ?
				AND clicod = ?;
			""", LocalDate.class, articleClient.artint(), articleClient.clicod()));
	}

	@Override
	public void checkEtag(long comanda, long numero) {
		var partsExpectedVersion = RequestThread.etag(Entity.LINIA_COMANDA).split("#");
		if (partsExpectedVersion.length == 3) {
			var comandaRevisar = Long.valueOf(partsExpectedVersion[0]);
			var numeroLiniaRevisar = Long.valueOf(partsExpectedVersion[1]);
			var expectedVersion = partsExpectedVersion[2];
			if (!expectedVersion.isEmpty() && comanda==comandaRevisar && numero==numeroLiniaRevisar) {
				etag(comanda, numero).ifPresent(etag -> {
					if (!expectedVersion.equals(etag.versio()))
						throw new LiniaComandaChanged(etag);
				});
			}
		}
	}

	@Override
	public Optional<ETag> etag(long comanda, long numero) {
		try {
			var currentETag = jdbcAmes.queryForObject(
				"SELECT versio, usuari, datareg FROM comandes.linia_comanda WHERE comanda = ? AND numero = ? AND actual = true",  
				(rs, rowNum) -> {
					return ETag.of(rs.getTimestamp("datareg").toLocalDateTime(), rs.getString("versio"), rs.getString("usuari"));
				},
				comanda, numero
			);
			return Optional.ofNullable(currentETag);
		} catch (EmptyResultDataAccessException empty) {
			return Optional.empty();
		} 
	}

}

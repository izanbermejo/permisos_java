package ames.comercial.comandes.internal.infraestructure.comanda;

import ames.comercial.comandes.ComandesException.ComandaChanged;
import ames.comercial.comandes.internal.domain.comanda.Comanda;
import ames.comercial.comandes.internal.domain.comanda.ComandaPropsImpl;
import ames.comercial.comandes.internal.domain.comanda.DadesNormalitzat;
import ames.comercial.comandes.internal.infraestructure.comanda.mapper.ComandaRecord;
import ames.comercial.comandes.internal.infraestructure.comanda.mapper.ComandaRecordMapper;
import ames.comercial.server.ETag;
import ames.comercial.server.Json;
import ames.comercial.server.RequestThread;
import ames.comercial.server.RequestThread.Entity;
import ames.comercial.shared.Empresa;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ComandaRepositorySQL implements ComandaRepository {
	
	private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
	private @Autowired ObjectMapper jsonMapper;
	private Json json;
	
	@PostConstruct
	public void init () {
		json = new Json(jsonMapper);
	}	

	@Override
	public void save(Comanda c) {
		long codi = c.codi();
		remove(codi);
		
		var params = new HashMap<String, Object>();
		params.put("codi", codi);
		params.put("tipus", c.tipus());
		params.put("client", c.dades().client());
		params.put("client_nom", c.dades().clientNom());
		params.put("empresa", c.dades().empresa());
		params.put("data_alta", c.dades().dataAlta());
		params.put("informacio_client", json.serialize(c.informacioClient()));
		params.put("adresa", json.serialize(c.adresa()));
		params.put("informacio_enviament", json.serialize(c.informacioEnviament()));
		params.put("import_net", c.dadesNormalitzat().map(DadesNormalitzat::importNet).orElse(null));
		params.put("import_brut", c.dadesNormalitzat().map(DadesNormalitzat::importBrut).orElse(null));
		params.put("divisa", c.dadesNormalitzat().map(DadesNormalitzat::divisa).orElse(null));
		params.put("pes", c.dadesNormalitzat().map(DadesNormalitzat::pes).orElse(null));
		params.put("cost_transport", c.dadesNormalitzat().map(DadesNormalitzat::costTransport).orElse(null));
		params.put("tarifes", c.dadesNormalitzat().map(DadesNormalitzat::tarifes).map(json::serialize).orElse(null));
		params.put("dades_enviament_justificant", json.serialize(c.dadesEnviamentJustificant().orElse(null)));
		params.put("stock_seguretat", c.isStockSeguretat());
		params.put("num_adjunts", c.numAdjunts());
		params.put("servida", c.servida());
		params.put("servible", c.servible());
		params.put("usuari", c.usuari());
		params.put("etag", json.serialize(ETag.generate()));
		
		new SimpleJdbcInsert(jdbcAmes)
			.withSchemaName("comandes")	
			.withTableName("comanda")
			.execute(params);
	}

	@Override
	public Optional<Comanda> find(long codi) {
		try {
			var rowComanda = jdbcAmes.queryForObject("SELECT * FROM comandes.comanda WHERE codi = ?", new ComandaRecordMapper(json), codi);
			return Optional.of(buildComanda(rowComanda));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();			
		}
	}

	@Override
	public List<Comanda> find(List<Long> ids) {
		if (ids.isEmpty()) return List.of();

		String placeholders = ids.stream()
				.map(id -> "?")
				.collect(Collectors.joining(", "));
		String sql = "SELECT * FROM comandes.comanda WHERE codi IN (%s)".formatted(placeholders);

		return jdbcAmes.query(sql, new ComandaRecordMapper(json), ids.toArray())
				.stream()
				.map(this::buildComanda)
				.toList();
	}

	@Override
	public Optional<Comanda> findByComandaClient(String client, String comandaClient, Empresa empresa) {
		try {
			var rowComanda  = jdbcAmes.queryForObject("""
				SELECT * FROM comandes.comanda 
				WHERE client = ?
				AND empresa = ? 
				AND informacio_client->>'identificador' = ?
				ORDER BY codi DESC
				LIMIT 1;
				""", new ComandaRecordMapper(json), client, empresa.clau(), comandaClient.trim());
			return Optional.of(buildComanda(rowComanda));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public void updateNumAdjunts(long comanda) {
		jdbcAmes.update("""
				UPDATE comandes.comanda c
				SET num_adjunts = (
				    SELECT COUNT(*)
				    FROM comandes.adjunt a
				    WHERE a.comanda = c.codi
				)
				WHERE c.codi = ?;
				""", comanda);
	}

    private Comanda buildComanda(ComandaRecord rowComanda) {
		var props = ComandaPropsImpl.builder()
				.tipus(rowComanda.tipus())
				.dades(rowComanda.dades())
				.informacioClient(rowComanda.informacioClient())
				.adresa(rowComanda.adresa())
				.informacioEnviament(rowComanda.informacioEnviament())
				.dadesNormalitzat(rowComanda.dadesNormalitzat())
				.dadesEnviamentJustificant(rowComanda.dadesEnviamentJustificant())
				.stockSeguretat(rowComanda.stockSeguretat())
				.numAdjunts(rowComanda.numAdjunts())
				.servida(rowComanda.servida())
				.servible(rowComanda.servible())
				.usuari(rowComanda.usuari())
				.build();
		return new Comanda(rowComanda.codi(), props);
	}
	
	public boolean exists (long codi) {
		return jdbcAmes.queryForObject("SELECT EXISTS(SELECT 1 FROM comandes.comanda WHERE codi = ?)",	Boolean.class, codi);
	}
	
	private void remove (long codi) {
		jdbcAmes.update("DELETE FROM comandes.comanda WHERE codi = ?", codi);
	}

	@Override
	public void checkEtag (long codi) {
		var expectedVersion = RequestThread.etag(Entity.COMANDA);
		if (!expectedVersion.isEmpty()) {
			etag(codi).ifPresent(etag -> {
				if (!expectedVersion.equals(etag.versio()))
					throw new ComandaChanged(etag);
			});
		}
	}

	@Override
	public Optional<ETag> etag (long codi) {
		try {
			var type = new TypeReference<ETag>() {};
			var currentETag = jdbcAmes.queryForObject(
				"SELECT etag FROM comandes.comanda WHERE codi = ?",  
				(rs, rowNum) -> json.deserialize(rs.getString(1), type), 
				codi
			);
			return Optional.of(currentETag);
		} catch (EmptyResultDataAccessException empty) {
			return Optional.empty();
		} 
	}
	
}

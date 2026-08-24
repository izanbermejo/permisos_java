package ames.comercial.clients.internal.infraestructure.adjunt;

import ames.comercial.clients.internal.domain.Adjunt;
import ames.comercial.server.ETag;
import ames.comercial.server.Json;
import ames.comercial.server.SimpleJdbcUpsert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class AdjuntClientRepositorySQL implements AdjuntClientRepositoryDatabase {

	private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
	private @Autowired ObjectMapper jsonMapper;
	private Json json;

	@PostConstruct
	public void init () {
		json = new Json(jsonMapper);
	}

	@Override
	public void add(String codiAdjunt, Adjunt a) {
		var newEtag = ETag.generate();
		@SuppressWarnings("serial")
		var params = new HashMap<String,Object>() {{
			put("codi", codiAdjunt);
			put("client", a.client());
			put("nom_fitxer", a.nom());
			put("categoria", a.categoria().idCategoria());
			put("usuari", a.usuari());
			put("data", a.data());
			put("etag", json.serialize(newEtag));
		}};
		new SimpleJdbcUpsert(jdbcAmes)
			.withSchemaName("adjunts")
			.withTableName("adjunts_client")
		    .onConflictColumns("codi", "client")
			.execute(params);
	}

	@Override
	public void remove(String codiClient, String codiAdjunt) {
		jdbcAmes.update("DELETE FROM adjunts.adjunts_client WHERE (codi = ?) AND (client = ?)", codiAdjunt, codiClient);
	}

	@Override
	public Optional<Adjunt> find(String codiClient, String codiAdjunt) {
		try {
			return Optional.ofNullable(jdbcAmes.queryForObject("SELECT * FROM adjunts.adjunts_client WHERE (codi = ?) AND (client = ?)",
					new AdjuntMapper(), codiAdjunt, codiClient));
		} catch (EmptyResultDataAccessException empty) {
			return Optional.empty();
		}
	}

	@Override
	public List<String> findNomSimilar(String codiClient, String nomBase) {
		return jdbcAmes.query("""
				SELECT nom_fitxer FROM adjunts.adjunts_client WHERE client = ? AND nom_fitxer LIKE CONCAT(?, '%')
				""",
				(rs, rowNum) -> rs.getString("nom_fitxer"),
				codiClient, nomBase);
	}

}

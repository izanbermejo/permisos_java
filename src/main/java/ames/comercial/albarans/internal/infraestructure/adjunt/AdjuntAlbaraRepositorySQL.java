package ames.comercial.albarans.internal.infraestructure.adjunt;

import ames.comercial.albarans.internal.domain.Adjunt;
import ames.comercial.server.ETag;
import ames.comercial.server.Json;
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

@Repository
public class AdjuntAlbaraRepositorySQL implements AdjuntAlbaraRepositoryDatabase {

	private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
	private @Autowired ObjectMapper jsonMapper;
	private Json json;

	@PostConstruct
	public void init () {
		json = new Json(jsonMapper);
	}

	@Override
	public void add(String codiAdjunt, Adjunt a) {
		remove(a.albara(), codiAdjunt);
		var newEtag = ETag.generate();
		@SuppressWarnings("serial")
		var params = new HashMap<String,Object>() {{
			put("codi", codiAdjunt);
			put("albara", a.albara());
			put("nom_fitxer", a.nom());
			put("usuari", a.usuari());
			put("data", a.data());
			put("etag", json.serialize(newEtag));
		}};
		new SimpleJdbcInsert(jdbcAmes)
			.withSchemaName("albarans")
			.withTableName("adjunt")
			.execute(params);
	}

	@Override
	public void remove(long albara, String codiAdjunt) {
		jdbcAmes.update("DELETE FROM albarans.adjunt WHERE (codi = ?) AND (albara = ?)", codiAdjunt, albara);
	}

	@Override
	public Optional<Adjunt> find(long albara, String codiAdjunt) {
		try {
			return Optional.ofNullable(jdbcAmes.queryForObject("SELECT * FROM albarans.adjunt WHERE (codi = ?) AND (albara = ?)",
					new AdjuntMapper(), codiAdjunt, albara));
		} catch (EmptyResultDataAccessException empty) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Adjunt> findByNom(long albara, String nom) {
		try {
			return Optional.ofNullable(jdbcAmes.queryForObject("SELECT * FROM albarans.adjunt WHERE (nom_fitxer = ?) AND (albara = ?)",
					new AdjuntMapper(), nom, albara));
		} catch (EmptyResultDataAccessException empty) {
			return Optional.empty();
		}
	}

	@Override
	public List<String> findNomSimilar(long albara, String nomBase) {
		return jdbcAmes.query("""
				SELECT nom_fitxer FROM albarans.adjunt WHERE albara = ? AND nom_fitxer LIKE CONCAT(?, '%')
				""",
				(rs, rowNum) -> rs.getString("nom_fitxer"),
				albara, nomBase);
	}

	@Override
	public List<Adjunt> findByComanda(long albara) {
		return jdbcAmes.query("SELECT * FROM albarans.adjunt WHERE (albara = ?)", new AdjuntMapper(), albara);
	}

}

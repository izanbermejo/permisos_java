package ames.comercial.comandes.internal.infraestructure.adjunt;

import ames.comercial.comandes.internal.domain.Adjunt;
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
public class AdjuntComandaRepositorySQL implements AdjuntComandaRepositoryDatabase {

	private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
	private @Autowired ObjectMapper jsonMapper;
	private Json json;

	@PostConstruct
	public void init () {
		json = new Json(jsonMapper);
	}

	@Override
	public void add(String codiAdjunt, Adjunt a) {
		remove(a.comanda(), codiAdjunt);
		var newEtag = ETag.generate();
		@SuppressWarnings("serial")
		var params = new HashMap<String,Object>() {{
			put("codi", codiAdjunt);
			put("comanda", a.comanda());
			put("nom_fitxer", a.nom());
			put("usuari", a.usuari());
			put("data", a.data());
			put("etag", json.serialize(newEtag));
		}};
		new SimpleJdbcInsert(jdbcAmes)
			.withSchemaName("comandes")
			.withTableName("adjunt")
			.execute(params);
	}

	@Override
	public void remove(long comanda, String codiAdjunt) {
		jdbcAmes.update("DELETE FROM comandes.adjunt WHERE (codi = ?) AND (comanda = ?)", codiAdjunt, comanda);
	}

	@Override
	public Optional<Adjunt> find(long comanda, String codiAdjunt) {
		try {
			return Optional.ofNullable(jdbcAmes.queryForObject("SELECT * FROM comandes.adjunt WHERE (codi = ?) AND (comanda = ?)",
					new AdjuntMapper(), codiAdjunt, comanda));
		} catch (EmptyResultDataAccessException empty) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<Adjunt> findByNom(long comanda, String nom) {
		try {
			return Optional.ofNullable(jdbcAmes.queryForObject("SELECT * FROM comandes.adjunt WHERE (nom_fitxer = ?) AND (comanda = ?)",
					new AdjuntMapper(), nom, comanda));
		} catch (EmptyResultDataAccessException empty) {
			return Optional.empty();
		}
	}

	@Override
	public List<String> findNomSimilar(long comanda, String nomBase) {
		return jdbcAmes.query("""
				SELECT nom_fitxer FROM comandes.adjunt WHERE comanda = ? AND nom_fitxer LIKE CONCAT(?, '%')
				""",
				(rs, rowNum) -> rs.getString("nom_fitxer"),
				comanda, nomBase);
	}

	@Override
	public List<Adjunt> findByComanda(long comanda) {
		return jdbcAmes.query("SELECT * FROM comandes.adjunt WHERE (comanda = ?)", new AdjuntMapper(), comanda);
	}

}

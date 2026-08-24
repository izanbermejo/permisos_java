package ames.comercial.cache.empleats;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ReompleCacheEmpleats {

    static final Logger log = LogManager.getLogger(ReompleCacheEmpleats.class.getName());

    @Autowired ObtenirEmpleatsCache obtenirEmpleatsCache;
    @Autowired NamedParameterJdbcTemplate jdbcTemplate;

    public void executar() {
        log.info("INI - Refresh cache empleats");
        var listRegistres = obtenirEmpleatsCache.executar();
        // S'eliminen les taules temporals
        jdbcTemplate.getJdbcTemplate().execute("DROP TABLE IF EXISTS cache.cache_empleats_old");
        jdbcTemplate.getJdbcTemplate().execute("DROP TABLE IF EXISTS cache.cache_empleats_new");
        // Creació de la taula temporal a partir da la existent
        jdbcTemplate.getJdbcTemplate().execute("CREATE TABLE cache.cache_empleats_new (LIKE cache.cache_empleats INCLUDING ALL)");

        // INSERT dels registres
        String sql = """
                    INSERT INTO cache.cache_empleats_new (id, usufab, nom, cognoms, nom_unicode, cognoms_unicode,
                        email, data_baixa)
                    VALUES(:id, :usufab, :nom, :cognoms, :nom_unicode, :cognoms_unicode, :email, :data_baixa);
                """;
        List<MapSqlParameterSource> batchValues = new ArrayList<>();
        for (var r : listRegistres) {
            batchValues.add(new MapSqlParameterSource()
                    .addValue("id", r.id())
                    .addValue("usufab", r.usufab())
                    .addValue("nom", r.nom())
                    .addValue("cognoms", r.cognoms())
                    .addValue("nom_unicode", r.nomUnicode().orElse(null))
                    .addValue("cognoms_unicode", r.cognomsUnicode().orElse(null))
                    .addValue("email", r.email().orElse(null))
                    .addValue("data_baixa", r.dataBaixa().orElse(null)));
            if (batchValues.size() >= 1000) {
                jdbcTemplate.batchUpdate(sql, batchValues.toArray(new SqlParameterSource[0]));
                batchValues.clear();
            }
        }
        if (!batchValues.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchValues.toArray(new SqlParameterSource[0]));
            batchValues.clear();
        }
        jdbcTemplate.getJdbcTemplate().execute("""
            BEGIN;
            ALTER TABLE cache.cache_empleats RENAME TO cache_empleats_old;
            ALTER TABLE cache.cache_empleats_new RENAME TO cache_empleats;
            DROP TABLE IF EXISTS cache.cache_empleats_old;
            COMMIT;
            """);
        log.info("FI - Refresh cache empleats");
    }

}

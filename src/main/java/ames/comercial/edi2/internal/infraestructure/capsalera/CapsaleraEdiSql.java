package ames.comercial.edi2.internal.infraestructure.capsalera;

import ames.comercial.edi2.internal.domain.capsalera.CapsaleraEdi;
import ames.comercial.server.Json;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class CapsaleraEdiSql implements CapsaleraEdiRepository{

    @Autowired JdbcTemplate jdbcAmes;
    @Autowired private ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    @Override
    public void save(List<CapsaleraEdi> capsaleresEdi){
        capsaleresEdi.forEach(c -> save(c.idMissatge(), c.idCapsalera(), c));
    }

    private void save(long idMissatge, long idCapsalera, CapsaleraEdi missatge){
        var params = new HashMap<String, Object>();
        params.put("id_missatge", idMissatge);
        params.put("id_capsalera", idCapsalera);
        params.put("ca", json.serialize(missatge.ca()));
        params.put("cb", missatge.cb().map(json::serialize).orElse(null));
        params.put("cc", missatge.cc().map(json::serialize).orElse(null));
        params.put("cd", missatge.cd().map(json::serialize).orElse(null));
        params.put("ci", json.serialize(missatge.ci()));
        params.put("cp", missatge.cp().map(json::serialize).orElse(null));
        params.put("cq", missatge.cq().map(json::serialize).orElse(null));
        params.put("cf", missatge.cf().map(json::serialize).orElse(null));
        params.put("ct", json.serialize(missatge.ct()));

        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("edi2")
                .withTableName("capsalera")
                .execute(params);
    }

    @Override
    public List<CapsaleraEdi> obtenirCapsaleraEdi(long idMissatge){
        return jdbcAmes.query("""
                    SELECT *
                    FROM edi2.capsalera
                    WHERE id_missatge = ?;
            """, new CapsaleraEdiMapper(json), idMissatge);
    }

    @Override
    public Optional<CapsaleraEdi> obtenir (long idMissatge, long idCapsalera){
        return Optional.ofNullable(jdbcAmes.queryForObject("""
                SELECT *
                FROM edi2.capsalera
                WHERE id_missatge = ? and id_capsalera = ?;
            """, new CapsaleraEdiMapper(json), idMissatge, idCapsalera));
    }
}

package ames.comercial.entrades.internal.infraestructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InboxEntradesSql implements  InboxEntrades {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public boolean exists(String id) {
        return Boolean.TRUE.equals(jdbcAmes.queryForObject("SELECT EXISTS(SELECT 1 FROM entrades.inbox WHERE id = ?)", Boolean.class, id));
    }

    @Override
    public void save(String id, String contingut, OrigenEntrada origenEntrada) {
        var params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("contingut", contingut);
        params.put("origen", origenEntrada.name());

        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("entrades")
                .withTableName("inbox")
                .usingGeneratedKeyColumns("datareg")
                .execute(params);
    }

    @Override
    public void marcaProcessat(String id) {
        jdbcAmes.update("UPDATE entrades.inbox SET data_processat = now() WHERE id = ?", id);
    }

    @Override
    public void marcaError(String id, String error) {
        jdbcAmes.update("UPDATE entrades.inbox SET data_processat = now(), error = ? WHERE id = ?", error, id);
    }

    @Override
    public Map<String, String> obtenirPendents(OrigenEntrada origenEntrada) {
        return jdbcAmes.query(
                "SELECT id, contingut FROM entrades.inbox WHERE data_processat IS NULL AND origen = ?",
                rs -> {
                    Map<String, String> map = new HashMap<>();
                    while (rs.next()) {
                        map.put(rs.getString("id"), rs.getString("contingut"));
                    }
                    return map;
                }, origenEntrada.name()
        );
    }

}

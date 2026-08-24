package ames.comercial.entrades.internal.infraestructure;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AdresesErrorEntradaSql implements AdresesErrorEntrada {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public List<JSONObject> obtenir() {
        return jdbcAmes.query("""
                SELECT *
                FROM entrades.setup;
            """,
            (rs, rowNum) ->
                new JSONObject()
                    .put("emailsComercial", rs.getString("adreses_error_comercial"))
                    .put("emailsMagatzem", rs.getString("adreses_error_magatzem"))
        );
    }

    @Override
    public String obtenirComercial() {
        return jdbcAmes.queryForObject(
                "SELECT adreses_error_comercial FROM entrades.setup", String.class);
    }

    @Override
    public String obtenirMagatzem() {
        return jdbcAmes.queryForObject(
                "SELECT adreses_error_magatzem FROM entrades.setup", String.class);
    }

    @Override
    public void guardar(String emailComercial, String emailMagatzem) {
        String sql = "UPDATE entrades.setup SET adreses_error_comercial = ?, adreses_error_magatzem = ?";

        jdbcAmes.update(sql, emailComercial, emailMagatzem);
    }


}

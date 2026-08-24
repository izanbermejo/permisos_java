package ames.comercial.entrades.internal.infraestructure.magatzem;

import ames.comercial.entrades.internal.domain.ErrorEntradaMagatzem;
import ames.comercial.server.SimpleJdbcUpsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Repository
public class ErrorEntradaMagatzemRepositorySQL implements ErrorEntradaMagatzemRepository {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public void guardarError(String idEntrada, String magatzem) {
        var params = new HashMap<String,Object>() {{
            put("id_entrada_fabrica", idEntrada);
            put("magatzem", magatzem);
        }};

        new SimpleJdbcUpsert(jdbcAmes)
            .withSchemaName("entrades")
            .withTableName("notificacio_error_magatzem")
            .onConflictColumns("id_entrada_fabrica")
            .execute(params);
    }

    @Override
    public void marcarEnviat(String idEntrada) {
        String sql = "UPDATE entrades.notificacio_error_magatzem SET data_avis = ? WHERE id_entrada_fabrica = ?";

        jdbcAmes.update(sql, LocalDateTime.now(), idEntrada);
    }

    @Override
    public List<ErrorEntradaMagatzem> listarPendents() {
        return jdbcAmes.query("""
            SELECT *
            FROM entrades.notificacio_error_magatzem
            WHERE data_avis is null;
        """,new ErrorEntradaMagatzemMapper());
    }

}

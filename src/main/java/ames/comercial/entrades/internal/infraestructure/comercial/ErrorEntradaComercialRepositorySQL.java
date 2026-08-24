package ames.comercial.entrades.internal.infraestructure.comercial;

import ames.comercial.entrades.internal.domain.ErrorEntradaComercial;
import ames.comercial.server.SimpleJdbcUpsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Repository
public class ErrorEntradaComercialRepositorySQL implements ErrorEntradaComercialRepository {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public void guardarError(String idEntrada) {
        var params = new HashMap<String,Object>() {{
            put("id_entrada_fabrica", idEntrada);
        }};

        new SimpleJdbcUpsert(jdbcAmes)
            .withSchemaName("entrades")
            .withTableName("notificacio_error_comercial")
            .onConflictColumns("id_entrada_fabrica")
            .execute(params);
    }

    @Override
    public void marcarEnviat(String idEntrada) {
        String sql = "UPDATE entrades.notificacio_error_comercial SET data_avis = ? WHERE id_entrada_fabrica = ?";

        jdbcAmes.update(sql, LocalDateTime.now(), idEntrada);
    }

    @Override
    public List<ErrorEntradaComercial> listarPendents() {
        return jdbcAmes.query("""
            SELECT *
            FROM entrades.notificacio_error_comercial
            WHERE data_avis is null;
        """,new ErrorEntradaComercialMapper());
    }

}

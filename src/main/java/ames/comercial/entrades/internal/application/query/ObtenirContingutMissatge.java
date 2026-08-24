package ames.comercial.entrades.internal.application.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.ws.rs.QueryParam;
import java.sql.Types;

@Service
public class ObtenirContingutMissatge {
    @Autowired
    NamedParameterJdbcTemplate jdbc;

    public String executar(String idEntradaFabrica) {
        var params = new MapSqlParameterSource();
        params.addValue("idEntradaFabrica", idEntradaFabrica, Types.VARCHAR);
        return jdbc.queryForObject("""
                        SELECT contingut
                        FROM entrades.inbox
                        WHERE id = :idEntradaFabrica;
                """,
                params, String.class);
    }

    public static class BuscarMissatgesRequest {
        @QueryParam("idEntradaFabrica") String idEntradaFabrica;
    }
}

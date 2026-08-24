package ames.comercial.entrades.internal.application.query;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.ws.rs.QueryParam;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BuscarMissatges {
    @Autowired
    NamedParameterJdbcTemplate jdbc;

    public List<JSONObject> executar(BuscarMissatges.BuscarMissatgesRequest req) {
        var params = new MapSqlParameterSource();
        params.addValue("contingutMissatge", req.contingutMissatge, Types.VARCHAR)
                .addValue("dataInici", req.dataInici != null ? req.dataInici.atStartOfDay() : null, Types.TIMESTAMP)
                .addValue("dataFi", req.dataFi != null ? req.dataFi.plusDays(1).atStartOfDay() : null, Types.TIMESTAMP);
        return jdbc.query("""
                        SELECT id,  datareg, data_processat, origen
                        FROM entrades.inbox
                        WHERE
                            -- Per comanda (interna, de client i programa)
                            (
                                (:contingutMissatge is null or length(:contingutMissatge) = 0)
                                OR (contingut::varchar like '%' || :contingutMissatge || '%')
                            )
                            AND -- Per data inici
                            (
                                (:dataInici IS NULL) OR datareg >= :dataInici
                            )
                            AND -- Per data fi
                            (
                                (:dataFi IS NULL) OR datareg <= :dataFi
                            )
                        ORDER BY datareg DESC
                        LIMIT 100;
                """,
                params,
                (rs, rowNum) ->
                        new JSONObject()
                                .put("id", rs.getString("id"))
                                .put("datareg", rs.getTimestamp("datareg").toLocalDateTime())
                                .put("data_processat", Optional.ofNullable(rs.getTimestamp("data_processat")).map(Timestamp::toLocalDateTime).orElse(null))
                                .put("origen", rs.getString("origen"))
        );
    }

    public static class BuscarMissatgesRequest {
        @QueryParam("contingutMissatge") String contingutMissatge;
        @QueryParam("dataInici") LocalDate dataInici;
        @QueryParam("dataFi") LocalDate dataFi;
    }
}

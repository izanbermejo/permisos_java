package ames.comercial.edi2.internal.application.query;

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
public class BuscarArxius {
    @Autowired
    NamedParameterJdbcTemplate jdbc;

    public List<JSONObject> executar(BuscarArxiusRequest req) {
        var params = new MapSqlParameterSource();
        params.addValue("nomArxiu", req.nomArxiu, Types.VARCHAR)
                .addValue("contingutTXT", req.contingutTXT, Types.VARCHAR)
                .addValue("dataInici", req.dataInici != null ? req.dataInici.atStartOfDay() : null, Types.TIMESTAMP)
                .addValue("dataFi", req.dataFi != null ? req.dataFi.plusDays(1).atStartOfDay() : null, Types.TIMESTAMP);
        return jdbc.query("""
                        SELECT *
                        FROM edi2.inbox
                        WHERE
                        -- Per nom d'arxiu
                            (
                                (:nomArxiu is null or length(:nomArxiu) = 0)
                                OR (missatge::varchar like '%' || :nomArxiu || '%')
                                OR (nom_pdf::varchar like '%' || :nomArxiu || '%')
                            )
                            AND -- Per contingut del TXT
                            (
                                (:contingutTXT is null or length(:contingutTXT) = 0)
                                OR (contingut::varchar like '%' || :contingutTXT || '%')
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
                                .put("id", rs.getInt("id"))
                                .put("missatge", rs.getString("missatge"))
                                .put("path", rs.getString("path"))
                                .put("path_pdf", rs.getString("path_pdf"))
                                .put("datareg", rs.getTimestamp("datareg").toLocalDateTime())
                                .put("data_processat", Optional.ofNullable(rs.getTimestamp("data_processat")).map(Timestamp::toLocalDateTime).orElse(null))
                                .put("contingut", rs.getString("contingut"))
                                .put("error", rs.getString("error"))
        );
    }

    public static class BuscarArxiusRequest {
        @QueryParam("nomArxiu") String nomArxiu;
        @QueryParam("contingutTXT") String contingutTXT;
        @QueryParam("dataInici") LocalDate dataInici;
        @QueryParam("dataFi") LocalDate dataFi;
    }
}

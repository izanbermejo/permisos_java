package ames.comercial.edi2.internal.application.query;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.List;

@Service
public class ObtenirContingutTXTByMissatge {
    @Autowired
    NamedParameterJdbcTemplate jdbc;

    public List<JSONObject> executar(String idMissatge) {
        var params = new MapSqlParameterSource();
        params.addValue("idMissatge", Integer.parseInt(idMissatge), Types.INTEGER);
        return jdbc.query("""
                        SELECT contingut
                        FROM edi2.inbox
                        WHERE :idMissatge = id;
                """,
                params,
                (rs, rowNum) ->
                        new JSONObject()
                                .put("contingut", rs.getString("contingut"))
        );
    }
}

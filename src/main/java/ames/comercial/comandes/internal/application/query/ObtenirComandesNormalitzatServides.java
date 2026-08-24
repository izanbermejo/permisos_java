package ames.comercial.comandes.internal.application.query;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ObtenirComandesNormalitzatServides {

    @Autowired
    NamedParameterJdbcTemplate jdbc;

    public List<JSONObject> executar (String client, LocalDate dataInici, LocalDate dataFi, String comanda) {
        var params = new MapSqlParameterSource("client", client);
        params.addValue("comanda", comanda);
        params.addValue("dataInici", dataInici);
        params.addValue("dataFi", dataFi);
        return jdbc.query("""
                SELECT c.codi, c.data_alta, c.servida, 
                c.informacio_client ->> 'data' as "dataSolicitada", 
                c.informacio_client ->> 'identificador' as "comandaClient",
                c.dades_enviament_justificant ->> 'usuari' as "usuariJustificant",
                c.dades_enviament_justificant ->> 'data' as "dataJustificant",
                c.import_net, c.import_brut, c.divisa, c.pes, c.cost_transport, 
                c.num_adjunts, c.servible, c.usuari,
                coment.intern
                FROM comandes.comanda c
                LEFT JOIN comandes.comanda_comentaris coment ON c.codi = coment.comanda
                WHERE c.client = :client AND c.servida
                    -- Per identificador de comanda
                    AND (
                        (:comanda is null or length(:comanda) = 0)
                        or
                        (c.informacio_client ->> 'identificador'::varchar ilike '%' || :comanda || '%')
                    )
                    -- Per data
                    AND c.data_alta BETWEEN :dataInici AND :dataFi
                    AND c.tipus = 'NORMALITZAT'
                ORDER BY c.codi DESC
                LIMIT 100
                """, params, (rs, rowNum) ->
                new JSONObject()
                        .put("comanda", String.format("%07d", rs.getLong("codi")))
                        .put("dataAlta", rs.getDate("data_alta").toLocalDate())
                        .put("servida", rs.getBoolean("servida"))
                        .put("dataSolicitada", rs.getDate("dataSolicitada").toLocalDate())
                        .put("comandaClient", rs.getString("comandaClient"))
                        .put("usuariJustificant", rs.getString("usuariJustificant"))
                        .put("dataJustificant", safeRead(rs.getString("dataJustificant")))
                        .put("importNet", rs.getBigDecimal("import_net"))
                        .put("importBrut", rs.getBigDecimal("import_brut"))
                        .put("divisa", rs.getString("divisa"))
                        .put("pes",rs.getBigDecimal("pes"))
                        .put("costTransport", rs.getBigDecimal("cost_transport"))
                        .put("numAdjunts", rs.getInt("num_adjunts"))
                        .put("servible", rs.getString("servible"))
                        .put("usuari", rs.getString("usuari"))
                        .put("comentariIntern", rs.getString("intern"))
        );
    }

    private LocalDateTime safeRead(String s) {
        if (s == null)
            return null;
        return LocalDateTime.parse(s);
    }

}

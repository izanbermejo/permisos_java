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
public class ObtenirComandesEspecials {

    @Autowired
    NamedParameterJdbcTemplate jdbc;

    public List<JSONObject> executar (String client, LocalDate dataInici, LocalDate dataFi, String comanda, boolean includeServides) {
        var params = new MapSqlParameterSource("client", client);
        params.addValue("comanda", comanda);
        params.addValue("dataInici", dataInici);
        params.addValue("dataFi", dataFi);
        params.addValue("includeServides", includeServides);
        return jdbc.query("""
                SELECT c.codi, c.data_alta, c.empresa, c.servida,
                c.informacio_client ->> 'identificador' as "comandaClient",
                c.informacio_client ->> 'programa' as "programa",
                c.dades_enviament_justificant ->> 'usuari' as "usuariJustificant",
                c.dades_enviament_justificant ->> 'data' as "dataJustificant",
                c.stock_seguretat as "isStockSeguretat",
                c.num_adjunts, c.usuari
                FROM comandes.comanda c
                WHERE c.client = :client
                    -- Per identificador de comanda
                    AND (
                        (:comanda is null or length(:comanda) = 0)
                        or
                        (c.informacio_client ->> 'identificador'::varchar ilike '%' || :comanda || '%')
                    )
                    -- Per data
                    AND c.data_alta BETWEEN :dataInici AND :dataFi
                    -- Incloure servides
                    AND (:includeServides OR c.servida = false)
                    AND c.tipus = 'PROGRAMA'
                ORDER BY c.codi DESC
                LIMIT 100
                """, params, (rs, rowNum) ->
                new JSONObject()
                        .put("comanda", String.format("%07d", rs.getLong("codi")))
                        .put("empresa", rs.getString("empresa"))
                        .put("dataAlta", rs.getDate("data_alta").toLocalDate())
                        .put("servida", rs.getBoolean("servida"))
                        .put("comandaClient", rs.getString("comandaClient"))
                        .put("programa", rs.getString("programa"))
                        .put("usuariJustificant", rs.getString("usuariJustificant"))
                        .put("dataJustificant", safeRead(rs.getString("dataJustificant")))
                        .put("isStockSeguretat", rs.getBoolean("isStockSeguretat"))
                        .put("numAdjunts", rs.getInt("num_adjunts"))
                        .put("usuari", rs.getString("usuari"))
        );
    }

    private LocalDateTime safeRead(String s) {
        if (s == null)
            return null;
        return LocalDateTime.parse(s);
    }

}

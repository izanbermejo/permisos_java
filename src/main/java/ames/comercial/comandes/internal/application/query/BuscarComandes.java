package ames.comercial.comandes.internal.application.query;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.ws.rs.QueryParam;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

@Service
public class BuscarComandes {

    @Autowired
    NamedParameterJdbcTemplate jdbc;

    public List<JSONObject> executar(BuscarComandesRequest req) {
        var params = new MapSqlParameterSource();
        params.addValue("tipus", req.tipus, Types.VARCHAR)
            .addValue("client", req.client, Types.VARCHAR)
            .addValue("comandaPrograma", req.comandaPrograma, Types.VARCHAR)
            .addValue("usuari", req.usuari, Types.VARCHAR)
            .addValue("dataInici", req.dataInici, Types.DATE)
            .addValue("dataFi", req.dataFi, Types.DATE)
            .addValue("servible", req.servible.toArray(new String[0]), Types.ARRAY)
            .addValue("servida", req.servida, Types.BOOLEAN);
        return jdbc.query("""
                        SELECT codi, tipus, data_alta, empresa,
                        	client, client_nom, informacio_client->>'identificador' AS "comandaClient",
                        	servida, num_adjunts, usuari,
                        	informacio_client->>'programa' AS "programa",
                        	servible
                        FROM comandes.comanda c
                        WHERE
                            -- Per tipus de comanda
                            (
                                (:tipus IS null) OR (length(:tipus)=0) OR (tipus = :tipus)
                            )
                            AND -- Per client (codi i nom)
                            (
                                (:client IS NULL) OR (length(:client) = 0)
                                OR (client ILIKE '%' || :client || '%')
                                OR (client_nom ILIKE '%' || :client || '%')
                            )
                            AND -- Per comanda (interna, de client i programa)
                            (
                                (:comandaPrograma is null or length(:comandaPrograma) = 0)
                                OR (codi::varchar ilike '%' || :comandaPrograma || '%')
                                OR (LPAD(codi::varchar, 7, '0') ILIKE '%' || :comandaPrograma || '%')
                                OR (informacio_client->>'identificador' ilike '%' || :comandaPrograma || '%')
                                OR (informacio_client->>'programa' ilike '%' || :comandaPrograma || '%')
                            )
                            AND -- Per usuari
                            (
                                (:usuari IS NULL) OR (length(:usuari) = 0)
                                OR (usuari ILIKE '%' || :usuari || '%')
                            )
                            AND -- Per data inici
                            (
                                (:dataInici IS NULL) OR data_alta >= :dataInici
                            )
                            AND -- Per data fi
                            (
                                (:dataFi IS NULL) OR data_alta <= :dataFi
                            )
                            AND -- Servible
                            (
                                (:servible is null or cardinality(:servible)=0)
                                OR (servible = any(:servible))
                            )
                            AND -- Servida
                            (	:servida is null
                                OR
                                (:servida and servida)
                                OR
                                (NOT :servida AND NOT servida)
                            )
                        ORDER BY codi DESC
                        LIMIT 100;
                """,
                params,
                (rs, rowNum) ->
                    new JSONObject()
                    .put("comanda", String.format("%07d", rs.getLong("codi")))
                    .put("tipus", rs.getString("tipus"))
                    .put("dataAlta", rs.getDate("data_alta").toLocalDate())
                    .put("codiClient", rs.getString("client"))
                    .put("nomClient", rs.getString("client_nom"))
                    .put("empresa",rs.getString("empresa"))
                    .put("comandaClient", rs.getString("comandaClient"))
                    .put("programa", rs.getString("programa"))
                    .put("servida",rs.getBoolean("servida"))
                    .put("servible", rs.getString("servible"))
                    .put("numAdjunts",rs.getLong("num_adjunts"))
                    .put("usuari",rs.getString("usuari"))
        );
    }

    public static class BuscarComandesRequest {
        @QueryParam("tipus") String tipus;
        @QueryParam("client") String client;
        @QueryParam("usuari") String usuari;
        @QueryParam("dataInici") LocalDate dataInici;
        @QueryParam("dataFi") LocalDate dataFi;
        @QueryParam("comandaPrograma") String comandaPrograma;
        @QueryParam("servible") List<String> servible;
        @QueryParam("servida") Boolean servida;
    }

}

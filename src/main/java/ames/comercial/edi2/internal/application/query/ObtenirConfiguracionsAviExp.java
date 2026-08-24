package ames.comercial.edi2.internal.application.query;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.List;

@Service
public class ObtenirConfiguracionsAviExp {

    @Autowired NamedParameterJdbcTemplate jdbc;

    public List<JSONObject> executar() {
        return executar("", Integer.MAX_VALUE);
    }

    public List<JSONObject> executar(String filtre) {
        return executar(filtre, Integer.MAX_VALUE);
    }

    public List<JSONObject> executar(String filtre, int limit) {
        var params = new MapSqlParameterSource();
        params.addValue("filtre", filtre, Types.VARCHAR);
        params.addValue("limit", limit);
        return jdbc.query("""
                        SELECT conf.*, cc.nom, cc.estat
                        FROM edi2.configuracio_client_aviexp conf
                        LEFT JOIN cache.cache_client cc ON cc.clicod = conf.clicod
                        WHERE
                            (
                                (:filtre is null or length(:filtre) = 0)
                                OR
                                (
                                    (conf.clicod::varchar like '%' || :filtre || '%')
                                    OR
                                    (cc.nom::varchar like '%' || :filtre || '%')
                                    OR
                                    (conf.codi_proveidor::varchar like '%' || :filtre || '%')
                                )
                            )
                        LIMIT :limit;
                """,
                params,
                (rs, rowNum) -> new JSONObject()
                        .put("codiClient", rs.getString("clicod"))
                        .put("nomClient", rs.getString("nom"))
                        .put("codiProveidor", rs.getString("codi_proveidor"))
                        .put("estat", rs.getString("estat"))
                        .put("volAviExp", rs.getBoolean("vol_aviexp"))
                        .put("volEnviarLG", rs.getBoolean("vol_enviar_lg_numero_linia"))
        );
    }
}

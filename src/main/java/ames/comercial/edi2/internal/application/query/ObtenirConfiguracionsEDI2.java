package ames.comercial.edi2.internal.application.query;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Array;
import java.sql.Types;
import java.util.List;

@Service
public class ObtenirConfiguracionsEDI2 {
    @Autowired
    NamedParameterJdbcTemplate jdbc;

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
                        FROM edi2.configuracio_client conf
                        LEFT JOIN cache.cache_client cc ON cc.clicod = conf.codi_client
                        WHERE
                            (
                                (:filtre is null or length(:filtre) = 0)
                                OR
                                (
                                    (conf.codi_client::varchar like '%' || :filtre || '%')
                                    OR
                                    (cc.nom::varchar like '%' || :filtre || '%')
                                    OR
                                    (conf.edibox::varchar like '%' || :filtre || '%')
                                    OR
                                    (conf.nad02::varchar like '%' || :filtre || '%')
                                    OR
                                    (conf.codi_proveidor::varchar like '%' || :filtre || '%')
                                )
                            )
                        LIMIT :limit;
                """,
                params,
                (rs, rowNum) -> {
                    Array arrayLlocEntrega = rs.getArray("lloc_entrega");
                    String[] llocEntrega = arrayLlocEntrega != null ? (String[]) arrayLlocEntrega.getArray() : null;

                    Array arrayDiesSortida = rs.getArray("dies_sortida");
                    Integer[] diesSortida = arrayDiesSortida != null ? (Integer[]) arrayDiesSortida.getArray() : null;

                    return new JSONObject()
                            .put("codiClient", rs.getString("codi_client"))
                            .put("nomClient", rs.getString("nom"))
                            .put("tipusMissatge", rs.getString("tipus_missatge"))
                            .put("fermOrientatiu", rs.getString("ferm_orientatiu"))
                            .put("ediBox", rs.getString("edibox"))
                            .put("nad02", rs.getString("nad02"))
                            .put("codiProveidor", rs.getString("codi_proveidor"))
                            .put("estrategiaEdi", rs.getString("estrategia_edi"))
                            .put("llocEntrega", llocEntrega != null ? new JSONArray(llocEntrega) : null)
                            .put("clauConfiguracio", concatenarClau(
                                    rs.getString("codi_client"),
                                    rs.getString("tipus_missatge")
                            ))
                            .put("estat", rs.getString("estat"))
                            .put("informacioSortida", informacioSortida((diesSortida != null ? new JSONArray(diesSortida) : null), rs.getInt("dies_restar")))
                            .put("considerarAlbarans", rs.getBoolean("considerar_albarans"))
                            .put("considerarDuesDates", rs.getBoolean("considerar_dues_dates"))
                            .put("diesTall", rs.getInt("dies_tall"))
                            .put("tipusDocumentEdi", rs.getString("tipus_document_edi"))
                            .put("comentaris", rs.getString("comentaris"))
                            .put("isActiu", rs.getBoolean("is_actiu"));
                }
        );
    }

    private String concatenarClau(String codiClient, String tipusMissatge) {
        return codiClient + "_" + tipusMissatge;
    }

    private JSONObject informacioSortida(JSONArray diesSortida, int diesRestar) {
        return new JSONObject()
                .put("diesSortida", diesSortida)
                .put("diesRestar", diesRestar);
    }
}

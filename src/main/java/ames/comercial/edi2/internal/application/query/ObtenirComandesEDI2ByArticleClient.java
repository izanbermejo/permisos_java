package ames.comercial.edi2.internal.application.query;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.util.List;

@Service
public class ObtenirComandesEDI2ByArticleClient {
    @Autowired
    NamedParameterJdbcTemplate jdbc;

    public List<JSONObject> executar(String codiClient) {
        var params = new MapSqlParameterSource();
        params.addValue("articleClient", codiClient, Types.VARCHAR);
        return jdbc.query("""
                        SELECT ca.ca->>'documento' as tipus, i.datareg as dataArribada, co.la->>'idArticuloComprador' as referenciaClient,
                            co.clicod, cc.nom as client, cac.codi_fabrica, ca.ca->>'numeroDocumento' as numeroDocument, co.id_capsalera,
                            co.id_comanda, cc.responsable, co.id_missatge, i.path_pdf, (path_pdf IS NOT NULL) AS te_pdf, co.estat, co.error
                        FROM edi2.comanda co
                        LEFT JOIN edi2.capsalera ca ON ca.id_missatge = co.id_missatge
                            AND ca.id_capsalera = co.id_capsalera
                        LEFT JOIN edi2.inbox i ON i.id = ca.id_missatge
                        LEFT JOIN cache.cache_client cc ON cc.clicod = co.clicod
                        LEFT JOIN cache.cache_article_client cac ON cac.artint = co.artint
                            AND cac.clicod = co.clicod
                        WHERE
                        (
                            :articleClient LIKE cac.codi_fabrica || co.clicod
                        )
                        AND
                        (
                            co.estat = 'ERROR' or co.estat = 'PENDENT_PROCESSAR'
                        )
                        ORDER BY tipus DESC, numeroDocument ASC;
                """,
                params,
                (rs, rowNum) ->
                     new JSONObject()
                    .put("tipus", rs.getString("tipus"))
                    .put("dataArribada", rs.getTimestamp("dataArribada").toLocalDateTime())
                    .put("referenciaClient", rs.getString("referenciaClient"))
                    .put("codiClient", rs.getString("clicod"))
                    .put("client", rs.getString("client"))
                    .put("articleClient", concatenarArticleClient(
                            rs.getString("codi_fabrica"),
                            rs.getString("clicod")
                    ))
                    .put("numeroDocument", rs.getString("numeroDocument"))
                    .put("idMissatge", rs.getString("id_missatge"))
                    .put("idCapsalera", rs.getString("id_capsalera"))
                    .put("idComanda", rs.getString("id_comanda"))
                    .put("responsable", rs.getString("responsable"))
                    .put("clauComanda", concatenarClau(
                            rs.getString("id_comanda"),
                            rs.getString("id_capsalera"),
                            rs.getString("id_missatge")
                    ))
                    .put("tePdf", rs.getBoolean("te_pdf"))
                    .put("pathPDF", rs.getString("path_pdf"))
                    .put("error", rs.getString("error"))
        );
    }

    private String concatenarClau(String idComanda, String idCapsalera, String idMissatge) {
        return idComanda + "_" + idCapsalera + "_" + idMissatge;
    }

    private String concatenarArticleClient(String codiArticle, String codiClient) {
        if (codiArticle == null || codiClient == null) { return ""; }
        return codiArticle + codiClient ;
    }

}

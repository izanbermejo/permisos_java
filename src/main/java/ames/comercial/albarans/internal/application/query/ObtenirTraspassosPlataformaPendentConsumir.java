package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.internal.domain.albara.InformacioTraspas;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.albarans.internal.infraestructure.linia.mapper.LiniaAlbaraMapper;
import ames.comercial.server.Json;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Obté les línies dels albarans de traspàs a plataforma ({@code TRASPAS_PLATAFORMA}) amb pendent de
 * consumir &gt; 0 per a una peça (articleClient) i empresa concretes en un magatzem plataforma,
 * ORDENADES per criteri FIFO (data i codi d'albarà, i número de línia). Es retorna també el magatzem
 * i la data de l'albarà de traspàs, necessaris per registrar la traçabilitat del consum.
 * <p>
 * Només compten els traspassos <b>tancats i entregats</b>: fins que la mercaderia no ha arribat a la
 * plataforma no s'hi pot consumir ni retornar. És el mateix criteri del Delphi
 * ({@code where a.albtancat='S' and a.entregat='S'}, mantalb.pas:3596).
 * <p>
 * El magatzem receptor (plataforma) es llegeix de la informació de traspàs (JSON) de la capçalera i
 * es filtra en memòria, seguint el mateix patró que {@link ObtenirAlbaransTraspasObertsAmbLinies}.
 */
@Service
public class ObtenirTraspassosPlataformaPendentConsumir {

    @Autowired NamedParameterJdbcTemplate jdbcAmes;
    private @Autowired ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init() {
        json = new Json(jsonMapper);
    }

    public List<LiniaTraspasPendent> executar(String magatzemPlataforma, String empresa, KeyArticleClient articleClient) {
        var params = new MapSqlParameterSource()
                .addValue("empresa", empresa)
                .addValue("artint", articleClient.artint())
                .addValue("clicod", articleClient.clicod());
        String sql = """
                SELECT l.*, a.magatzem AS traspas_magatzem, a.data AS traspas_data, a.informacio_traspas
                FROM albarans.linia_albara l
                JOIN albarans.albara a ON a.codi = l.codi_albara AND a.empresa = l.empresa
                WHERE a.tipus = 'TRASPAS_PLATAFORMA'
                    AND l.empresa = :empresa
                    AND l.artint = :artint
                    AND l.clicod = :clicod
                    AND l.quantitat_pendent_consumir > 0
                    AND a.is_tancat
                    AND COALESCE((a.informacio_magatzem ->> 'isEntregat')::boolean, false)
                ORDER BY a.data, a.codi, l.linia
                """;
        return jdbcAmes.query(sql, params, (ResultSet rs) -> extractData(rs, magatzemPlataforma));
    }

    private List<LiniaTraspasPendent> extractData(ResultSet rs, String magatzemPlataforma) throws SQLException {
        var mapper = new LiniaAlbaraMapper();
        List<LiniaTraspasPendent> resultat = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            var infoTraspas = json.deserialize(rs.getString("informacio_traspas"), InformacioTraspas.class);
            if (infoTraspas != null && magatzemPlataforma.equals(infoTraspas.magatzemReceptor())) {
                resultat.add(new LiniaTraspasPendent(
                        mapper.mapRow(rs, rowNum++),
                        rs.getString("traspas_magatzem"),
                        rs.getDate("traspas_data").toLocalDate()));
            }
        }
        return resultat;
    }

    /** Línia de traspàs a plataforma pendent de consumir, amb el magatzem i la data del seu albarà. */
    public record LiniaTraspasPendent(LiniaAlbara linia, String magatzemTraspas, LocalDate dataTraspas) {}

}

package ames.comercial.tarifes.application.query;

import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class ObtenirPreusTarifa {

    private @Autowired NamedParameterJdbcTemplate jdbcNamingAmes;

    public Optional<ObtenirPreusTarifaResponse> executar (Set<KeyArticleClient> articles, String tarifaNormalitzats, BigDecimal factorAplicar) {
        var strDivisa = obtenirDivisa(tarifaNormalitzats);
        if (strDivisa.isEmpty())
            return Optional.empty();
        var divisa = Divisa.getBySymbol(strDivisa.get());
        if (articles.isEmpty())
            return Optional.of(new ObtenirPreusTarifaResponse(divisa, Map.of()));
        // Obtenció dels trams de la tarifa
        var trams = obtenirTrams(tarifaNormalitzats);
        RangeMap<Long, Preu> preusQuantitat = TreeRangeMap.create();
        // Obtenció dels preus
        var params = new MapSqlParameterSource();
        params.addValue("artint", articles.stream().map(KeyArticleClient::artint).toList());
        params.addValue("tarifaNormalitzat", tarifaNormalitzats);
        var preus = jdbcNamingAmes.query("""
                        SELECT p.* FROM tarifes_normalitzats.preu p join tarifes_normalitzats.tarifa t on p.codi_tarifa=t.codi
                        WHERE 
                        (LENGTH(:tarifaNormalitzat)>0 AND t.nom = :tarifaNormalitzat) 
                        AND p.artint IN (:artint)
                """, params,
                rs -> {
                    var res = new HashMap<String, Map<Integer, Preu>>();
                    while (rs.next()) {
                        var artint = rs.getString("artint");
                        var prods = new HashMap<Integer, Preu>();
                        prods.put(1, Preu.of(rs.getBigDecimal("pr01").multiply(factorAplicar), divisa));
                        prods.put(2, Preu.of(rs.getBigDecimal("pr02").multiply(factorAplicar), divisa));
                        prods.put(3, Preu.of(rs.getBigDecimal("pr03").multiply(factorAplicar), divisa));
                        prods.put(4, Preu.of(rs.getBigDecimal("pr04").multiply(factorAplicar), divisa));
                        prods.put(5, Preu.of(rs.getBigDecimal("pr05").multiply(factorAplicar), divisa));
                        prods.put(6, Preu.of(rs.getBigDecimal("pr06").multiply(factorAplicar), divisa));
                        prods.put(7, Preu.of(rs.getBigDecimal("pr07").multiply(factorAplicar), divisa));
                        prods.put(8, Preu.of(rs.getBigDecimal("pr08").multiply(factorAplicar), divisa));
                        prods.put(9, Preu.of(rs.getBigDecimal("pr09").multiply(factorAplicar), divisa));
                        prods.put(10, Preu.of(rs.getBigDecimal("pr10").multiply(factorAplicar), divisa));
                        prods.put(11, Preu.of(rs.getBigDecimal("pr11").multiply(factorAplicar), divisa));
                        prods.put(12, Preu.of(rs.getBigDecimal("pr12").multiply(factorAplicar), divisa));
                        res.put(artint, prods);
                    }
                    return res;
        });
        // Creació de la resposta
        Map<String, RangeMap<Long, Preu>> tarifes = new HashMap<>();
        preus.forEach((artint, p) -> {
            tarifes.put(artint, buildRangeMap(trams, p));
        });

        return Optional.of(new ObtenirPreusTarifaResponse(divisa, tarifes));
    }

    private RangeMap<Long, Preu> buildRangeMap (Map<Integer, Long> trams, Map<Integer, Preu> preus) {
        // Creació del rang de preus
        RangeMap<Long, Preu> resultat = TreeRangeMap.create();
        // Trams intermitjos
        int i;
        for (i=2; i <=12; i++) {
            resultat.put(Range.closedOpen(trams.get(i-1), trams.get(i)), preus.get(i-1));
            // En cas que l'actual sigui igual a l'anterior no cal seguir fent trams
            if (trams.get(i-1) == trams.get(i))
                break;
        }
        // S'afegeix l'últim valor
        resultat.put(Range.atLeast(trams.get(i-1)), preus.get(i-1));
        return resultat;
    }

    private Optional<String> obtenirDivisa (String tarifa) {
        try {
            var params = new MapSqlParameterSource();
            params.addValue("tarifa", tarifa);
            return Optional.ofNullable(jdbcNamingAmes.queryForObject("SELECT divisa FROM tarifes_normalitzats.tarifa WHERE nom = :tarifa", params, String.class));
        } catch (EmptyResultDataAccessException e) {
         return Optional.empty();
        }
    }

    private Map<Integer, Long> obtenirTrams(String tarifa) {
        var params = new MapSqlParameterSource();
        params.addValue("tarifa", tarifa);
        return jdbcNamingAmes.query("""
                        SELECT * FROM tarifes_normalitzats.tarifa WHERE nom = :tarifa
                """, params,
                rs -> {
                    var res = new HashMap<Integer, Long>();
                    if (rs.next()) {
                        for (var i = 1; i<=12; i++)
                            res.put(i, rs.getLong("tram"+i));
                    }
                    return res;
                });
    }

    public record ObtenirPreusTarifaResponse (Divisa divisa, Map<String, RangeMap<Long, Preu>> preus) {};

}

package ames.comercial.inventari.internal.application.query;

import ames.comercial.inventari.internal.application.query.ObtenirInformacioInventari.ObtenirInformacioInventariResponse.ObtenirInformacioInventariDataPrimerUltimMoviment;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class ObtenirInformacioInventari {

    @Autowired NamedParameterJdbcTemplate jdbcTemplate;

    public Optional<ObtenirInformacioInventariResponse> executar(KeyArticleClient articleClient) {
        var params = new MapSqlParameterSource("artint", articleClient.artint())
                .addValue("clicod", articleClient.clicod());
        // Execució de la query per obtenir les relacions d'empreses i magatzems amb les dates dels primers i últims moviments
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT empresa, magatzem, MIN(data) AS primer_moviment, MAX(data) AS ultim_moviment
                FROM inventari.moviment
                WHERE artint = :artint AND clicod = :clicod
                GROUP BY empresa, magatzem
                """, params);
        // En cas que no es trobin moviments, retornem un Optional buit ja que no hi ha informació d'inventari
        if (rows.isEmpty()) {
            return Optional.empty();
        }

        // Construcció de la resposta a partir de les dades obtingudes
        return Optional.of(toResponse(rows));
    }

    private ObtenirInformacioInventariResponse toResponse(List<Map<String, Object>> rows) {
        // Creació de cada Map de correspondència
        Map<String, Map<String, ObtenirInformacioInventariDataPrimerUltimMoviment>> empresesMap = new HashMap<>();
        Map<String, Map<String, ObtenirInformacioInventariDataPrimerUltimMoviment>> magatzemsMap = new HashMap<>();

        for (Map<String, Object> row : rows) {
            // Mapeig de les columnes de la query
            String empresa = (String) row.get("empresa");
            String magatzem = (String) row.get("magatzem");
            LocalDate primer = ((java.sql.Date) row.get("primer_moviment")).toLocalDate();
            LocalDate ultim = ((java.sql.Date) row.get("ultim_moviment")).toLocalDate();

            var datesMoviments = ObtenirInformacioInventariDataPrimerUltimMovimentImpl.builder()
                            .primerMoviment(primer)
                            .ultimMoviment(ultim)
                            .build();

            // Map d'empreses a magatzem
            empresesMap.computeIfAbsent(empresa, k -> new HashMap<>()).put(magatzem, datesMoviments);

            // Map de magatzems a empreses
            magatzemsMap.computeIfAbsent(magatzem, k -> new HashMap<>()).put(empresa, datesMoviments);
        }

        return ObtenirInformacioInventariResponseImpl.builder()
                .empreses(empresesMap)
                .magatzems(magatzemsMap)
                .build();
    }


    @JsonDeserialize(builder = ObtenirInformacioInventariResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirInformacioInventariResponse {
        Map<String, Map<String, ObtenirInformacioInventariDataPrimerUltimMoviment>> empreses();
        Map<String, Map<String, ObtenirInformacioInventariDataPrimerUltimMoviment>> magatzems();

        @Derived
        default LocalDate primerMoviment() {
            return Stream.concat(empreses().values().stream(),
                                magatzems().values().stream())
                    .flatMap(m -> m.values().stream())
                    .map(ObtenirInformacioInventariDataPrimerUltimMoviment::primerMoviment)
                    .min(LocalDate::compareTo)
                    .orElse(null);
        }

        @Derived
        default LocalDate ultimMoviment() {
            return Stream.concat(empreses().values().stream(),
                            magatzems().values().stream())
                    .flatMap(m -> m.values().stream())
                    .map(ObtenirInformacioInventariDataPrimerUltimMoviment::primerMoviment)
                    .max(LocalDate::compareTo)
                    .orElse(null);
        }

        @JsonDeserialize(builder = ObtenirInformacioInventariDataPrimerUltimMovimentImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface ObtenirInformacioInventariDataPrimerUltimMoviment {
            LocalDate primerMoviment();
            LocalDate ultimMoviment();
        }

    }

}

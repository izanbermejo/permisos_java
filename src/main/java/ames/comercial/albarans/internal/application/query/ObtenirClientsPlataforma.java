package ames.comercial.albarans.internal.application.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Obté els clients que tenen stock en un magatzem plataforma, per poder triar de quin client és el
 * consum abans de crear-ne la capçalera.
 * <p>
 * L'stock de la plataforma ({@code inventari.fitxa}) és per empresa i client, de manera que el
 * resultat són parelles (empresa, client): un mateix client hi pot tenir stock de més d'una empresa i
 * llavors apareix una vegada per empresa. Això fixa alhora el client i l'empresa de l'albarà, que és
 * l'empresa del numerador d'albarans.
 */
@Service
public class ObtenirClientsPlataforma {

    @Autowired NamedParameterJdbcTemplate jdbcAmes;

    public List<ClientPlataformaResponse> executar(String magatzemPlataforma) {
        var params = new MapSqlParameterSource().addValue("magatzem", magatzemPlataforma);
        String sql = """
                SELECT DISTINCT f.empresa, f.clicod, c.nom
                FROM inventari.fitxa f
                LEFT JOIN cache.cache_client c
                    ON c.clicod = f.clicod
                WHERE f.magatzem = :magatzem
                    AND f.stock <> 0
                ORDER BY c.nom, f.clicod, f.empresa
                """;
        return jdbcAmes.query(sql, params, (rs, rowNum) -> ClientPlataformaResponseImpl.builder()
                .empresa(rs.getString("empresa"))
                .clicod(rs.getString("clicod"))
                .nom(Optional.ofNullable(rs.getString("nom")).orElse(""))
                .build());
    }

    @JsonDeserialize(builder = ClientPlataformaResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ClientPlataformaResponse {
        /** Empresa propietària de l'stock; serà l'empresa de l'albarà de consum */
        String empresa();
        String clicod();
        String nom();
    }

}

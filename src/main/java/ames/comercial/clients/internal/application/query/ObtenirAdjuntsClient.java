package ames.comercial.clients.internal.application.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.ws.rs.QueryParam;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirAdjuntsClient {

    @Autowired @Qualifier("jdbcAmes")
    JdbcTemplate jdbcAmes;

    public List<ObtenirAdjuntsClientResponse> executar(String codiClient, ObtenirAdjuntsClientRequest adjuntsClient) {

        var params = new MapSqlParameterSource("codiClient", codiClient)
                .addValue("categoria", adjuntsClient.categoria.orElse(null))
                .addValue("nomFitxer", adjuntsClient.nomFitxer.orElse(""));

        return new NamedParameterJdbcTemplate(jdbcAmes).query(
                """
                        SELECT *
                        FROM adjunts.adjunts_client
                        WHERE
                            (
                                client = :codiClient
                            )
                            AND
                            (
                                :categoria::integer is null
                                OR categoria = :categoria
                            )
                            AND
                            (
                                (:nomFitxer is null or length(:nomFitxer) = 0)
                                OR (nom_fitxer::varchar like '%' || :nomFitxer || '%')
                            )
                        ORDER BY data DESC;
                """,
                params,
                this::mapAllAdjuntsClient
        );
    }

    private ObtenirAdjuntsClientResponse mapAllAdjuntsClient(ResultSet rs, int rowNum) throws SQLException {
        return ObtenirAdjuntsClientResponseImpl.builder()
                .codi(rs.getString("codi"))
                .client(rs.getString("client"))
                .nomFitxer(rs.getString("nom_fitxer"))
                .categoria(rs.getInt("categoria"))
                .usuari(rs.getString("usuari"))
                .data(rs.getTimestamp("data").toLocalDateTime())
                .build();
    }

    public static class ObtenirAdjuntsClientRequest {
        @QueryParam("categoria") Optional<Integer> categoria;
        @QueryParam("nomFitxer") Optional<String> nomFitxer;
    }

    @JsonDeserialize(builder = ObtenirAdjuntsClientResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirAdjuntsClientResponse {

        String codi();
        String client();
        String nomFitxer();
        long categoria();
        String usuari();
        LocalDateTime data();
    }
}
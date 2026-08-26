package ames.permisos.aplicacions.internal.application.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class ObtenirAplicacions {

    @Autowired @Qualifier("jdbcAmes")
    JdbcTemplate jdbcAmes;

    public List<ObtenirAplicacionsResponse> executar() {

        return new NamedParameterJdbcTemplate(jdbcAmes).query(
                """
                        SELECT nom_aplicacio, descripcio
                        FROM organigrama_permisos.aplicacio;
                """,
                this::mapAllAplicacions
        );
    }

    private ObtenirAplicacionsResponse mapAllAplicacions(ResultSet rs, int rowNum) throws SQLException {
        return ObtenirAplicacionsResponseImpl.builder()
                .nomAplicacio(rs.getString("nom_aplicacio"))
                .descripcio(rs.getString("descripcio"))
                .build();
    }

    @JsonDeserialize(builder = ObtenirAplicacionsResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirAplicacionsResponse {

        String nomAplicacio();
        String descripcio();
    }
}
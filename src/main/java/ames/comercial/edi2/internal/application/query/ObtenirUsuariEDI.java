package ames.comercial.edi2.internal.application.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class ObtenirUsuariEDI implements IObtenirUsuarisEDI {
    @Autowired
    JdbcTemplate jdbcAmes;

    @Override
    public List<ObtenirUsuariEDIResponse> all() {
        return new NamedParameterJdbcTemplate(jdbcAmes).query(
                """
                        SELECT ep.id_empleat, ch.responsable
                        FROM edi2.configuracio_client cc
                        JOIN cache.cache_client ch ON ch.clicod = cc.codi_client
                        JOIN organigrama_permisos.empleat_parametre ep ON ep.valor = ch.responsable
                        GROUP BY ch.responsable, ep.id_empleat;
                """,
                this::mapAllUsuarisEDI
        );
    }

    private ObtenirUsuariEDIResponse mapAllUsuarisEDI(ResultSet rs, int rowNum) throws SQLException {
        return ObtenirUsuariEDIResponseImpl.builder()
                .idResponsable(rs.getString("id_empleat"))
                .responsable(rs.getString("responsable"))
                .build();
    }

    @JsonDeserialize(builder = ObtenirUsuariEDIResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirUsuariEDIResponse {

        String idResponsable();
        String responsable();

    }
}

package ames.permisos.permisos.internal.application.query;

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
public class ObtenirPermisosFuncio {

    @Autowired @Qualifier("jdbcAmes")
    JdbcTemplate jdbcAmes;

    public List<ObtenirPermisosFuncioResponse> executar() {

        return new NamedParameterJdbcTemplate(jdbcAmes).query(
                """
                        SELECT *
                        FROM organigrama_permisos.funcio_permis;
                """,
                this::mapAllPermisosFuncio
        );
    }

    private ObtenirPermisosFuncioResponse mapAllPermisosFuncio(ResultSet rs, int rowNum) throws SQLException {
        return ames.permisos.permisos.internal.application.query.ObtenirPermisosFuncioResponseImpl.builder()
                .nomAplicacio(rs.getString("nom_aplicacio"))
                .nomModul(rs.getString("nom_modul"))
                .nomPermis(rs.getString("nom_permis"))
                .idCentre(rs.getInt("id_centre"))
                .idDepartament(rs.getInt("id_departament"))
                .idFuncio(rs.getInt("id_funcio"))
                .build();
    }

    @JsonDeserialize(builder = ObtenirPermisosFuncioResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirPermisosFuncioResponse {

        String nomAplicacio();
        String nomModul();
        String nomPermis();
        long idCentre();
        long idDepartament();
        long idFuncio();
    }
}
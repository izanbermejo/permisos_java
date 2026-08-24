package ames.comercial.cache.empleats;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Llegeix els empleats de l'origen que els manté (schema organigrama del PostgreSQL d'Ames).
 * A diferència de les altres caches, l'origen no és l'Advantage sinó una altra taula del mateix
 * PostgreSQL, de manera que la lectura es fa amb el JdbcTemplate d'Ames.
 */
@Component
public class ObtenirEmpleatsCache {

    @Autowired JdbcTemplate jdbcAmes;

    public List<RegistreCacheEmpleat> executar() {
        return jdbcAmes.query("""
                    SELECT id, usufab, nom, cognoms, nom_unicode, cognoms_unicode, email, data_baixa
                    FROM organigrama.empleats
                """,
                (rs, rowNum) -> RegistreCacheEmpleatImpl.builder()
                        .id(rs.getLong("id"))
                        .usufab(rs.getLong("usufab"))
                        .nom(readOptionalString(rs, "nom").orElse(""))
                        .cognoms(readOptionalString(rs, "cognoms").orElse(""))
                        .nomUnicode(readOptionalString(rs, "nom_unicode"))
                        .cognomsUnicode(readOptionalString(rs, "cognoms_unicode"))
                        .email(readOptionalString(rs, "email"))
                        .dataBaixa(readOptionalDate(rs, "data_baixa"))
                        .build());
    }

    private Optional<String> readOptionalString(ResultSet rs, String column) throws SQLException {
        var s = rs.getString(column);
        return Strings.isNullOrEmpty(s) ? Optional.empty() : Optional.of(s);
    }

    private Optional<LocalDate> readOptionalDate(ResultSet rs, String column) throws SQLException {
        var d = rs.getDate(column);
        return rs.wasNull() ? Optional.empty() : Optional.of(d.toLocalDate());
    }

    @JsonDeserialize(builder = RegistreCacheEmpleatImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface RegistreCacheEmpleat {
        long id();
        /** Codi de l'empleat a fàbrica (el mateix que al Keycloak. */
        long usufab();
        String nom();
        String cognoms();
        Optional<String> nomUnicode();
        Optional<String> cognomsUnicode();
        Optional<String> email();
        /** Informada només si l'empleat ja no és a l'empresa. */
        Optional<LocalDate> dataBaixa();
    }

}

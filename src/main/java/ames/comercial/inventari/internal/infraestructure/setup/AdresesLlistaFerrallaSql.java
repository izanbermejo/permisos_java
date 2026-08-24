package ames.comercial.inventari.internal.infraestructure.setup;

import com.google.common.base.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdresesLlistaFerrallaSql implements AdresesLlistaFerralla {

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

    /**
     * Es llegeix amb {@code query} i no amb {@code queryForObject} perquè si la taula no té cap
     * fila la tasca programada no ha de petar: ja avisa que no hi ha adreces configurades.
     */
    @Override
    public String obtenir() {
        return jdbcAmes.query("""
                SELECT adreses_llista_ferralla
                FROM inventari.setup
                """, (rs, rowNum) -> Strings.nullToEmpty(rs.getString("adreses_llista_ferralla")))
                .stream()
                .findFirst()
                .orElse("");
    }

}

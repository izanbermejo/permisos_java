package ames.comercial.edi2.internal.application.query;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObtenirSenseArtIntCliCod {

    @Autowired NamedParameterJdbcTemplate jdbc;

    public List<ComandaSenseArtint> executar() {
        return jdbc.query("""
            SELECT DISTINCT cap.ca ->> 'buzonOrigen' as edibox,
            com.lc ->> 'codigoConsignatario' as nad02,
            cap.ci ->> 'idProveedor' as codiProveidor,
            cap.ca ->> 'documento' as tipoMissatge,
            com.la ->> 'idArticuloComprador' as idArticleComprador,
            com.id_comanda,
            com.id_missatge
            FROM edi2.capsalera cap
            LEFT JOIN edi2.comanda com on com.id_missatge = cap.id_missatge
                AND com.id_capsalera = cap.id_capsalera
            WHERE com.artint is null OR com.clicod is null
            """,
                new MapSqlParameterSource(),
                (rs, rowNum) -> ComandaSenseArtintImpl.builder()
                    .edibox(rs.getString("edibox"))
                    .nad02(rs.getString("nad02"))
                    .codiProveidor(rs.getString("codiProveidor"))
                    .tipoMissatge(rs.getString("tipoMissatge"))
                    .idArticleComprador(rs.getString("idArticleComprador"))
                    .idComanda(rs.getLong("id_comanda"))
                    .idMissatge(rs.getLong("id_missatge"))
                    .build());
    }

    @JsonDeserialize(builder = ComandaSenseArtintImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ComandaSenseArtint {
        long idComanda();
        long idMissatge();
        String edibox();
        String nad02();
        String codiProveidor();
        String tipoMissatge();
        String idArticleComprador();
    }
}


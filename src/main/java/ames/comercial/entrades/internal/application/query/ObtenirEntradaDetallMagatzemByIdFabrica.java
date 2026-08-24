package ames.comercial.entrades.internal.application.query;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirEntradaDetallMagatzemByIdFabrica {

    @Autowired JdbcTemplate jdbcAmes;

    public List<DetallEntradaMagatzemResponse> executar(String id) {
        var sql = """
                SELECT *
                FROM entrades.entrada_magatzem_detall
                WHERE id = ?;
                """;
        return jdbcAmes.query(sql, this::mapRow, id);
    }

    public DetallEntradaMagatzemResponse mapRow(ResultSet rs, int rowNum) throws SQLException {
        return DetallEntradaMagatzemResponseImpl.builder()
                .id(rs.getString("id"))
                .etiquetaCaixa(rs.getLong("etiqueta_caixa"))
                .quantitat(rs.getLong("quantitat"))
                .dataEtiqueta(rs.getDate("data_etiqueta").toLocalDate())
                .lot(rs.getString("lot"))
                .of(rs.getLong("of"))
                .error(rs.getString("error") != null)
                .msgError(Optional.ofNullable(rs.getString("error")))
                .build();
    }

    @JsonDeserialize(builder = DetallEntradaMagatzemResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface DetallEntradaMagatzemResponse {
        String id();
        long etiquetaCaixa();
        long quantitat();
        LocalDate dataEtiqueta();
        Optional<String> lot();
        long of();
        boolean error();
        Optional<String> msgError();
    }

}


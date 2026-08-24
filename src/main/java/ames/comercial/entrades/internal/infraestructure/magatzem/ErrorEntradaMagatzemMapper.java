package ames.comercial.entrades.internal.infraestructure.magatzem;

import ames.comercial.entrades.internal.domain.ErrorEntradaMagatzem;
import ames.comercial.entrades.internal.domain.ErrorEntradaMagatzemImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class ErrorEntradaMagatzemMapper implements RowMapper<ErrorEntradaMagatzem> {

    @Override
    public ErrorEntradaMagatzem mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ErrorEntradaMagatzemImpl.builder()
                .idEntradaFabrica(rs.getString("id_entrada_fabrica"))
                .magatzem(rs.getString("magatzem"))
                .dataError(rs.getTimestamp("data_error").toLocalDateTime())
                .dataAvis(Optional.ofNullable(rs.getTimestamp("data_avis")).map(Timestamp::toLocalDateTime))
                .build();
    }

}

package ames.comercial.entrades.internal.infraestructure.comercial;

import ames.comercial.entrades.internal.domain.ErrorEntradaComercial;
import ames.comercial.entrades.internal.domain.ErrorEntradaComercialImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class ErrorEntradaComercialMapper implements RowMapper<ErrorEntradaComercial> {

    @Override
    public ErrorEntradaComercial mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ErrorEntradaComercialImpl.builder()
                .idEntradaFabrica(rs.getString("id_entrada_fabrica"))
                .dataError(rs.getTimestamp("data_error").toLocalDateTime())
                .dataAvis(Optional.ofNullable(rs.getTimestamp("data_avis")).map(Timestamp::toLocalDateTime))
                .build();
    }

}

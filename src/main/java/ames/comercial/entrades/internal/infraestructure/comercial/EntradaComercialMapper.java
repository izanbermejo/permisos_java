package ames.comercial.entrades.internal.infraestructure.comercial;

import ames.comercial.entrades.internal.domain.EntradaComercial;
import ames.comercial.entrades.internal.domain.EntradaComercialImpl;
import ames.comercial.server.MapperUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EntradaComercialMapper implements RowMapper<EntradaComercial> {

    @Override
    public EntradaComercial mapRow(ResultSet rs, int rowNum) throws SQLException {
        return EntradaComercialImpl.builder()
                .id(rs.getString("id"))
                .idEntradaFabrica(rs.getString("id_entrada_fabrica"))
                .articleFabrica(rs.getString("article"))
                .client(rs.getString("client"))
                .magatzem(rs.getString("magatzem"))
                .fabrica(rs.getString("fabrica"))
                .quantitat(rs.getLong("quantitat"))
                .quantitatCaixa(rs.getLong("quantitat_caixa"))
                .of(rs.getLong("of"))
                .dataEntrada(rs.getDate("data_entrada").toLocalDate())
                .pesPremsat(rs.getBigDecimal("pes_premsat"))
                .pesFinal(rs.getBigDecimal("pes_final"))
                .dataAlta(rs.getTimestamp("data_alta").toLocalDateTime())
                .dataProcessat(MapperUtils.readOptionalLocalDateTime(rs,"data_processat"))
                .error(MapperUtils.readOptionalString(rs, "error"))
                .build();
    }

}

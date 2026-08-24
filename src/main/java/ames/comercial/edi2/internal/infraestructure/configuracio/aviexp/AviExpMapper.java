package ames.comercial.edi2.internal.infraestructure.configuracio.aviexp;

import ames.comercial.edi2.internal.domain.ConfiguracioAviExp;
import ames.comercial.edi2.internal.domain.ConfiguracioAviExpImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class AviExpMapper implements RowMapper<ConfiguracioAviExp> {


    public AviExpMapper() {}

    @Override
    public ConfiguracioAviExp mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ConfiguracioAviExpImpl.builder()
                .codiClient(rs.getString("clicod"))
                .codiProveidor(rs.getString("codi_proveidor"))
                .volAviExp(rs.getBoolean("vol_aviexp"))
                .volEnviarLG(rs.getBoolean("vol_enviar_lg_numero_linia"))
                .ediBox(Optional.ofNullable(rs.getString("edibox")))
                .build();
    }
}

package ames.comercial.edi2.internal.application.query;

import ames.comercial.edi2.internal.domain.ConfiguracioAviExp;
import ames.comercial.edi2.internal.domain.ConfiguracioAviExpImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Service
public class CarregaConfiguracioAviExp {

    @Autowired NamedParameterJdbcTemplate jdbc;

    /**
     * Mètode per retornar la configuració d'un client per l'AviExp
     * @param codiClient, @param codiProveidor
     * @return ConfiguracioAviExp
     */

    public Optional<ConfiguracioAviExp> get(String codiClient, String codiProveidor){
        String sql = """
            SELECT *
            FROM edi2.configuracio_client_aviexp
            WHERE clicod = :codiClient
            AND codi_proveidor = :codiProveidor;
        """;

        var params = new MapSqlParameterSource()
                .addValue("codiClient", codiClient)
                .addValue("codiProveidor", codiProveidor);

        var result = jdbc.query(
                sql,
                params,
                this::mapConfiguracioAviExp
        );

        return result.stream().findFirst();
    }

    private ConfiguracioAviExp mapConfiguracioAviExp(ResultSet rs, int rowNum) throws SQLException {
        return ConfiguracioAviExpImpl.builder()
                .codiClient(rs.getString("clicod"))
                .codiProveidor(rs.getString("codi_proveidor"))
                .volAviExp(rs.getBoolean("vol_aviexp"))
                .volEnviarLG(rs.getBoolean("vol_enviar_lg_numero_linia"))
                .ediBox(rs.getString("edibox"))
                .build();
    }

}

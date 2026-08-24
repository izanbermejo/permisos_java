package ames.comercial.edi2.internal.infraestructure.configuracio.aviexp;

import ames.comercial.edi2.internal.domain.ConfiguracioAviExp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AviExpSql implements AviExpRepository {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public void save(ConfiguracioAviExp configuracioAviExp) {
        //UPSERT

        String sql = """
        INSERT INTO edi2.configuracio_client_aviexp (
            clicod,
            codi_proveidor,
            vol_aviexp,
            vol_enviar_lg_numero_linia,
            edibox
        )
        VALUES (
            :clicod,
            :codiProveidor,
            :volAviExp,
            :volEnviarLG,
            :edibox
        )
        ON CONFLICT (clicod)
        DO UPDATE SET
            codi_proveidor = EXCLUDED.codi_proveidor,
            vol_aviexp = EXCLUDED.vol_aviexp,
            vol_enviar_lg_numero_linia = EXCLUDED.vol_enviar_lg_numero_linia,
            edibox = EXCLUDED.edibox
    """;

        var params = new MapSqlParameterSource()
                .addValue("clicod", configuracioAviExp.codiClient())
                .addValue("codiProveidor", configuracioAviExp.codiProveidor())
                .addValue("volAviExp", configuracioAviExp.volAviExp())
                .addValue("volEnviarLG", configuracioAviExp.volEnviarLG())
                .addValue("edibox", configuracioAviExp.ediBox().orElse(null));

        new NamedParameterJdbcTemplate(jdbcAmes).update(sql, params);
    }

    @Override
    public ConfiguracioAviExp obtenirConfiguracio(String codiClient) {
        return jdbcAmes.queryForObject("""
                    SELECT *
                    FROM edi2.configuracio_client_aviexp
                    WHERE clicod = ?;
                """, new AviExpMapper(), codiClient);
    }
}

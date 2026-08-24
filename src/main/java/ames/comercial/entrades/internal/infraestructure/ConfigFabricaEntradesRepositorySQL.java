package ames.comercial.entrades.internal.infraestructure;

import ames.comercial.entrades.internal.domain.ConfigFabricaEntrades;
import ames.comercial.entrades.internal.domain.ConfigFabricaEntradesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ConfigFabricaEntradesRepositorySQL implements ConfigFabricaEntradesRepository {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public Optional<ConfigFabricaEntrades> get(String codiFabrica) {
        return Optional.ofNullable(jdbcAmes.queryForObject(
                "SELECT codi_fabrica, magatzem, empresa FROM entrades.config_fabrica WHERE codi_fabrica = ?",
                (rs, i) -> ConfigFabricaEntradesImpl.builder()
                        .codiFabrica(rs.getString("codi_fabrica"))
                        .magatzem(rs.getString("magatzem"))
                        .empresa(rs.getString("empresa"))
                        .build(),
                codiFabrica));
    }

}

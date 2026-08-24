package ames.comercial.entrades.internal.infraestructure;

import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ConfigArticleClientEntradesRepositorySQL implements ConfigArticleClientEntradesRepository {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public Optional<String> getEmpresa(KeyArticleClient articleClient, String codiFabrica) {
        try {
            return Optional.ofNullable(jdbcAmes.queryForObject(
                    "SELECT empresa FROM entrades.config_articleclient WHERE artint = ? AND clicod = ? AND codi_fabrica = ?",
                    String.class,
                    articleClient.artint(), articleClient.clicod(), codiFabrica));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}

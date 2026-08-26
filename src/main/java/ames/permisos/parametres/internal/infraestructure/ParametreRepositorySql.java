package ames.permisos.parametres.internal.infraestructure;

import ames.permisos.parametres.internal.domain.Parametre;
import ames.permisos.parametres.internal.infraestructure.mapper.ParametreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ParametreRepositorySql implements ParametreRepository {

    private @Autowired JdbcTemplate jdbcAmes;

    @Override
    public List<Parametre> obtenirParametresByAplicacio(String nomAplicacio) {
        return jdbcAmes.query(
                """
                        SELECT *
                        FROM organigrama_permisos.parametre
                        WHERE nom_aplicacio = ?;
                """, new ParametreMapper(), nomAplicacio
        );
    }
}

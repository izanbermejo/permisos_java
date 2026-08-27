package ames.permisos.permisos.internal.infraestructure;

import ames.permisos.permisos.internal.domain.Permis;
import ames.permisos.permisos.internal.infraestructure.mapper.PermisMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PermisRepositorySql implements PermisRepository {

    private @Autowired JdbcTemplate jdbcAmes;

    @Override
    public List<Permis> obtenirPermisosByAplicacioModul(String nomAplicacio, String nomModul) {
        return jdbcAmes.query(
                """
                        SELECT *
                        FROM organigrama_permisos.permis
                        WHERE nom_aplicacio = ? AND nom_modul = ?;
                """, new PermisMapper(), nomAplicacio, nomModul
        );
    }
}

package ames.permisos.moduls.internal.infraestructure;

import ames.permisos.moduls.internal.domain.Modul;
import ames.permisos.moduls.internal.infraestructure.mapper.ModulMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ModulRepositorySql implements ModulRepository {

    private @Autowired JdbcTemplate jdbcAmes;

    @Override
    public List<Modul> obtenirModulsByAplicacio(String nomAplicacio) {
        return jdbcAmes.query(
                """
                        SELECT *
                        FROM organigrama_permisos.modul
                        WHERE nom_aplicacio = ?;
                """, new ModulMapper(), nomAplicacio
        );
    }
}

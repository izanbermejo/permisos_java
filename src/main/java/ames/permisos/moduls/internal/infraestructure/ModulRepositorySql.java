package ames.permisos.moduls.internal.infraestructure;

import ames.permisos.moduls.internal.domain.Modul;
import ames.permisos.moduls.internal.infraestructure.mapper.ModulMapper;
import ames.permisos.server.SimpleJdbcUpsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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

    @Override
    public void save(String nomAplicacio, String nomModul, String descripcio) {
        var params = new HashMap<String, Object>();
        params.put("nom_aplicacio", nomAplicacio);
        params.put("nom_modul", nomModul);
        params.put("descripcio", descripcio);
        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("organigrama_permisos")
                .withTableName("modul")
                .onConflictColumns("nom_aplicacio", "nom_modul")
                .execute(params);
    }

    @Override
    public void delete(String nomAplicacio, String nomModul) {
        jdbcAmes.update("DELETE FROM organigrama_permisos.modul WHERE nom_aplicacio = ? AND nom_modul = ?", nomAplicacio, nomModul);
    }

    @Override
    public Optional<Modul> find(String nomAplicacio, String nomModul) {
        return jdbcAmes.query("""
                SELECT nom_aplicacio, nom_modul, descripcio
                FROM organigrama_permisos.modul
                WHERE nom_aplicacio = ? AND nom_modul = ?;
            """, new ModulMapper(), nomAplicacio, nomModul).stream().findFirst();
    }
}

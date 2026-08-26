package ames.permisos.aplicacions.internal.infraestructure;

import ames.permisos.aplicacions.internal.domain.Aplicacio;
import ames.permisos.aplicacions.internal.infraestructure.mapper.AplicacioMapper;
import ames.permisos.server.SimpleJdbcUpsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Optional;

@Repository
public class AplicacioRepositorySql implements AplicacioRepository{

    private @Autowired JdbcTemplate jdbcAmes;

    @Override
    public void save(String nomAplicacio, String descripcio) {
        var params = new HashMap<String, Object>();
        params.put("nom_aplicacio", nomAplicacio);
        params.put("descripcio", descripcio);
        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("organigrama_permisos")
                .withTableName("aplicacio")
                .onConflictColumns("nom_aplicacio")
                .execute(params);
    }

    @Override
    public void delete(String nomAplicacio) {
        jdbcAmes.update("DELETE FROM organigrama_permisos.aplicacio WHERE nom_aplicacio = ?", nomAplicacio);
    }

    @Override
    public boolean estaAssignada(String nomAplicacio) {
         var count = jdbcAmes.queryForObject("""
                SELECT COUNT(*)
                FROM organigrama_permisos.modul
                WHERE nom_aplicacio = ?;
                """, Integer.class, nomAplicacio);

        count += jdbcAmes.queryForObject("""
                SELECT COUNT(*)
                FROM organigrama_permisos.parametre
                WHERE nom_aplicacio = ?;
                """, Integer.class, nomAplicacio);

         return count != null && count > 0;
    }

    @Override
    public Optional<Aplicacio> find(String nomAplicacio) {
        return Optional.ofNullable(jdbcAmes.queryForObject("""
                        SELECT nom_aplicacio, descripcio
                        FROM organigrama_permisos.aplicacio
                        WHERE nom_aplicacio = ?;
                """, new AplicacioMapper(), nomAplicacio));
    }
}

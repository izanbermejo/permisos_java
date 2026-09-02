package ames.permisos.moduls.internal.infraestructure;

import ames.permisos.moduls.internal.domain.Modul;
import ames.permisos.moduls.internal.infraestructure.mapper.ModulMapper;
import ames.permisos.organigrama.internal.domain.Empleat;
import ames.permisos.organigrama.internal.domain.Funcio;
import ames.permisos.organigrama.internal.infraestructure.mapper.EmpleatAssignacioMapper;
import ames.permisos.organigrama.internal.infraestructure.mapper.EmpleatMapper;
import ames.permisos.organigrama.internal.infraestructure.mapper.FuncioMapper;
import ames.permisos.server.Json;
import ames.permisos.server.SimpleJdbcUpsert;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ModulRepositorySql implements ModulRepository {

    private @Autowired ObjectMapper jsonMapper;
    private Json json;
    private TypeReference<Map<String,String>> mapType;
    private @Autowired JdbcTemplate jdbcAmes;
    @Autowired NamedParameterJdbcTemplate jdbc;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
        this.mapType = new TypeReference<Map<String,String>>() {};
    }

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

    @Override
    public List<Funcio> listFuncioDelModul(String nomAplicacio, String nomModul) {
        return jdbcAmes.query("""
                SELECT fm.id_centre, c.nom AS nom_centre, fm.id_departament,
                        d.descripcio AS nom_departament, fm.id_funcio, f.descripcio AS nom_funcio
                FROM organigrama_permisos.funcio_modul fm
                LEFT JOIN organigrama.centres c ON c.id = fm.id_centre
                LEFT JOIN organigrama.departament d ON d.id = fm.id_departament
                LEFT JOIN organigrama.funcio f ON f.id = fm.id_funcio
                WHERE fm.nom_aplicacio = ? AND fm.nom_modul = ?;
            """, new FuncioMapper(json), nomAplicacio, nomModul);
    }

    @Override
    public Optional<Funcio> findAssignacioFuncio(String nomAplicacio, String nomModul, Funcio funcio) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomModul", nomModul)
                .addValue("idCentre", funcio.idCentre().orElse(null), Types.INTEGER)
                .addValue("idDepartament", funcio.idDepartament().orElse(null), Types.INTEGER)
                .addValue("idFuncio", funcio.idFuncio().orElse(null), Types.INTEGER);

        Optional<Funcio> funcioAssignada = jdbc.query("""
            SELECT fm.*, c.nom AS nom_centre, fm.id_departament,
                d.descripcio AS nom_departament, fm.id_funcio, f.descripcio AS nom_funcio
            FROM organigrama_permisos.funcio_modul fm
            LEFT JOIN organigrama.centres c ON c.id = fm.id_centre
            LEFT JOIN organigrama.departament d ON d.id = fm.id_departament
            LEFT JOIN organigrama.funcio f ON f.id = fm.id_funcio
            WHERE fm.nom_aplicacio = :nomAplicacio AND fm.nom_modul = :nomModul
            AND (
                  fm.id_centre = :idCentre
                  OR (fm.id_centre IS NULL AND :idCentre IS NULL)
              )
              AND (
                  fm.id_departament = :idDepartament
                  OR (fm.id_departament IS NULL AND :idDepartament IS NULL)
              )
              AND (
                  fm.id_funcio = :idFuncio
                  OR (fm.id_funcio IS NULL AND :idFuncio IS NULL)
              );
        """, params, new FuncioMapper(json)).stream().findFirst();

        return funcioAssignada;
    }

    @Override
    public void saveAssignacioFuncio(String nomAplicacio, String nomModul, Funcio funcio) {
        var params = new HashMap<String, Object>();
        params.put("nom_aplicacio", nomAplicacio);
        params.put("nom_modul", nomModul);
        params.put("id_centre", funcio.idCentre().orElse(null));
        params.put("id_departament", funcio.idDepartament().orElse(null));
        params.put("id_funcio", funcio.idFuncio().orElse(null));
        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("organigrama_permisos")
                .withTableName("funcio_modul")
                .onConflictColumns("nom_aplicacio", "nom_modul", "id_centre", "id_departament", "id_funcio")
                .execute(params);
    }

    @Override
    public void deleteFuncio(String nomAplicacio, String nomModul, Funcio funcio) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomModul", nomModul)
                .addValue("idCentre", funcio.idCentre().orElse(null), Types.INTEGER)
                .addValue("idDepartament", funcio.idDepartament().orElse(null), Types.INTEGER)
                .addValue("idFuncio", funcio.idFuncio().orElse(null), Types.INTEGER);

        jdbc.update("""
            DELETE FROM organigrama_permisos.funcio_modul
            WHERE nom_aplicacio = :nomAplicacio
              AND nom_modul = :nomModul
              AND (
                  id_centre = :idCentre
                  OR (id_centre IS NULL AND :idCentre IS NULL)
              )
              AND (
                  id_departament = :idDepartament
                  OR (id_departament IS NULL AND :idDepartament IS NULL)
              )
              AND (
                  id_funcio = :idFuncio
                  OR (id_funcio IS NULL AND :idFuncio IS NULL)
              );
            """, params);

    }

    @Override
    public List<Empleat> listEmpleatDelModul(String nomAplicacio, String nomModul) {
        return jdbcAmes.query("""
                SELECT DISTINCT e.id, e.nom, e.cognoms, e.email
                FROM organigrama_permisos.empleat_modul em
                JOIN organigrama.empleats e ON e.id = em.id_empleat
                WHERE em.nom_aplicacio = ? AND em.nom_modul = ?;
            """, new EmpleatMapper(), nomAplicacio, nomModul);
    }

    @Override
    public Optional<Empleat> findAssignacioEmpleat(String nomAplicacio, String nomModul, int idEmpleat) {
        return jdbcAmes.query("""
                SELECT em.nom_aplicacio, em.nom_modul, e.id, e.nom, e.cognoms, e.email
                FROM organigrama_permisos.empleat_modul em
                JOIN organigrama.empleats e ON e.id = em.id_empleat
                WHERE em.nom_aplicacio = ? AND em.nom_modul = ? AND em.id_empleat = ?;
            """, new EmpleatMapper(), nomAplicacio, nomModul, idEmpleat).stream().findFirst();
    }

    @Override
    public void saveAssignacioEmpleat(String nomAplicacio, String nomModul, int idEmpleat) {
        var params = new HashMap<String, Object>();
        params.put("nom_aplicacio", nomAplicacio);
        params.put("nom_modul", nomModul);
        params.put("id_empleat", idEmpleat);
        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("organigrama_permisos")
                .withTableName("empleat_modul")
                .onConflictColumns("nom_aplicacio", "nom_modul", "id_empleat")
                .execute(params);
    }

    @Override
    public void deleteEmpleat(String nomAplicacio, String nomModul, int idEmpleat) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomModul", nomModul)
                .addValue("idEmpleat", idEmpleat);

        jdbc.update("""
            DELETE FROM organigrama_permisos.empleat_modul
            WHERE nom_aplicacio = :nomAplicacio
              AND nom_modul = :nomModul
              AND id_empleat = :idEmpleat
            """, params);

    }

    @Override
    public List<Empleat> listAllEmpleatDelModul(String nomAplicacio, String nomModul) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomModul", nomModul);

        return jdbc.query("""
                SELECT e.id, e.nom, e.cognoms, e.email, 'INDIVIDUAL' AS tipus_assignacio
                FROM organigrama_permisos.empleat_modul em
                JOIN organigrama.empleats e
                    ON e.id = em.id_empleat
                WHERE em.nom_aplicacio = :nomAplicacio
                  AND em.nom_modul = :nomModul
        
                UNION
        
                SELECT e.id, e.nom, e.cognoms, e.email, 'FUNCIO' AS tipus_assignacio
                FROM organigrama_permisos.funcio_modul fm
                JOIN organigrama.funcions_departament_centre fdc ON
                    (fm.id_centre IS NULL OR fm.id_centre = fdc.centre)
                    AND (fm.id_departament IS NULL OR fm.id_departament = fdc.departament)
                    AND (fm.id_funcio IS NULL OR fm.id_funcio = fdc.funcio)
                JOIN organigrama.empleat_funcio ef
                    ON ef.id_func_dep_cent = fdc.id
                JOIN organigrama.empleats e
                    ON e.id = ef.id_persona
                WHERE fm.nom_aplicacio = :nomAplicacio
                  AND fm.nom_modul = :nomModul
                ORDER BY id;
        """, params, new EmpleatAssignacioMapper());
    }
}

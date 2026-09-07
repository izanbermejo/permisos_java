package ames.permisos.permisos.internal.infraestructure;

import ames.permisos.organigrama.internal.domain.Empleat;
import ames.permisos.organigrama.internal.domain.Funcio;
import ames.permisos.organigrama.internal.infraestructure.mapper.EmpleatAssignacioMapper;
import ames.permisos.organigrama.internal.infraestructure.mapper.EmpleatMapper;
import ames.permisos.organigrama.internal.infraestructure.mapper.FuncioMapper;
import ames.permisos.permisos.internal.domain.Permis;
import ames.permisos.permisos.internal.infraestructure.mapper.PermisMapper;
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
public class PermisRepositorySql implements PermisRepository {

    private @Autowired ObjectMapper jsonMapper;
    private Json json;
    private TypeReference<Map<String,String>> mapType;
    private @Autowired JdbcTemplate jdbcAmes;
    @Autowired
    NamedParameterJdbcTemplate jdbc;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
        this.mapType = new TypeReference<Map<String,String>>() {};
    }

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

    @Override
    public Optional<Permis> find(String nomAplicacio, String nomModul, String nomPermis) {
        return jdbcAmes.query("""
                SELECT *
                FROM organigrama_permisos.permis
                WHERE nom_aplicacio = ? AND nom_modul = ? AND nom_permis = ?;
            """, new PermisMapper(), nomAplicacio, nomModul, nomPermis).stream().findFirst();
    }

    @Override
    public void save(Permis permis) {
        var params = new HashMap<String, Object>();
        params.put("nom_aplicacio", permis.nomAplicacio());
        params.put("nom_modul", permis.nomModul());
        params.put("nom_permis", permis.nomPermis());
        params.put("descripcio", permis.descripcio());
        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("organigrama_permisos")
                .withTableName("permis")
                .onConflictColumns("nom_aplicacio", "nom_modul", "nom_permis")
                .execute(params);
    }

    @Override
    public void delete(String nomAplicacio, String nomModul, String nomPermis) {
        jdbcAmes.update("DELETE FROM organigrama_permisos.permis WHERE nom_aplicacio = ? AND nom_modul = ? AND nom_permis = ?",
                nomAplicacio, nomModul, nomPermis);
    }

    @Override
    public List<Funcio> listFuncioDelPermis(String nomAplicacio, String nomModul, String nomPermis) {
        return jdbcAmes.query("""
                SELECT fp.id_centre, c.nom AS nom_centre, fp.id_departament,
                        d.descripcio AS nom_departament, fp.id_funcio, f.descripcio AS nom_funcio
                FROM organigrama_permisos.funcio_permis fp
                LEFT JOIN organigrama.centres c ON c.id = fp.id_centre
                LEFT JOIN organigrama.departament d ON d.id = fp.id_departament
                LEFT JOIN organigrama.funcio f ON f.id = fp.id_funcio
                WHERE fp.nom_aplicacio = ? AND fp.nom_modul = ? AND fp.nom_permis = ?;
            """, new FuncioMapper(json), nomAplicacio, nomModul, nomPermis);
    }

    @Override
    public void deleteFuncio(String nomAplicacio, String nomModul, String nomPermis, Funcio funcio) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomModul", nomModul)
                .addValue("nomPermis", nomPermis)
                .addValue("idCentre", funcio.idCentre().orElse(null), Types.INTEGER)
                .addValue("idDepartament", funcio.idDepartament().orElse(null), Types.INTEGER)
                .addValue("idFuncio", funcio.idFuncio().orElse(null), Types.INTEGER);

        jdbc.update("""
            DELETE FROM organigrama_permisos.funcio_permis
            WHERE nom_aplicacio = :nomAplicacio
              AND nom_modul = :nomModul
              AND nom_permis = :nomPermis
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
    public Optional<Funcio> findAssignacioFuncio(String nomAplicacio, String nomModul, String nomPermis, Funcio funcio) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomModul", nomModul)
                .addValue("nomPermis", nomPermis)
                .addValue("idCentre", funcio.idCentre().orElse(null), Types.INTEGER)
                .addValue("idDepartament", funcio.idDepartament().orElse(null), Types.INTEGER)
                .addValue("idFuncio", funcio.idFuncio().orElse(null), Types.INTEGER);

        return jdbc.query("""
            SELECT fp.*, c.nom AS nom_centre, fp.id_departament,
                d.descripcio AS nom_departament, fp.id_funcio, f.descripcio AS nom_funcio
            FROM organigrama_permisos.funcio_permis fp
            LEFT JOIN organigrama.centres c ON c.id = fp.id_centre
            LEFT JOIN organigrama.departament d ON d.id = fp.id_departament
            LEFT JOIN organigrama.funcio f ON f.id = fp.id_funcio
            WHERE fp.nom_aplicacio = :nomAplicacio AND fp.nom_modul = :nomModul AND fp.nom_permis = :nomPermis
            AND (
                  fp.id_centre = :idCentre
                  OR (fp.id_centre IS NULL AND :idCentre IS NULL)
              )
              AND (
                  fp.id_departament = :idDepartament
                  OR (fp.id_departament IS NULL AND :idDepartament IS NULL)
              )
              AND (
                  fp.id_funcio = :idFuncio
                  OR (fp.id_funcio IS NULL AND :idFuncio IS NULL)
              );
        """, params, new FuncioMapper(json)).stream().findFirst();
    }

    @Override
    public void saveAssignacioFuncio(String nomAplicacio, String nomModul, String nomPermis, Funcio funcio) {
        var params = new HashMap<String, Object>();
        params.put("nom_aplicacio", nomAplicacio);
        params.put("nom_modul", nomModul);
        params.put("nom_permis", nomPermis);
        params.put("id_centre", funcio.idCentre().orElse(null));
        params.put("id_departament", funcio.idDepartament().orElse(null));
        params.put("id_funcio", funcio.idFuncio().orElse(null));
        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("organigrama_permisos")
                .withTableName("funcio_permis")
                .onConflictColumns("nom_aplicacio", "nom_modul", "nom_permis", "id_centre", "id_departament", "id_funcio")
                .execute(params);
    }

    @Override
    public List<Empleat> listEmpleatDelPermis(String nomAplicacio, String nomModul, String nomPermis) {
        return jdbcAmes.query("""
                SELECT DISTINCT e.id, e.nom, e.cognoms, e.email
                FROM organigrama_permisos.empleat_permis ep
                JOIN organigrama.empleats e ON e.id = ep.id_empleat
                WHERE ep.nom_aplicacio = ? AND ep.nom_modul = ? AND ep.nom_permis = ?;
            """, new EmpleatMapper(), nomAplicacio, nomModul, nomPermis);
    }

    @Override
    public Optional<Empleat> findAssignacioEmpleat(String nomAplicacio, String nomModul, String nomPermis, int idEmpleat) {
        return jdbcAmes.query("""
                SELECT e.id, e.nom, e.cognoms, e.email
                FROM organigrama_permisos.empleat_permis ep
                JOIN organigrama.empleats e ON e.id = ep.id_empleat
                WHERE ep.nom_aplicacio = ? AND ep.nom_modul = ? AND ep.nom_permis = ? AND ep.id_empleat = ?;
            """, new EmpleatMapper(), nomAplicacio, nomModul, nomPermis, idEmpleat).stream().findFirst();
    }

    @Override
    public void saveAssignacioEmpleat(String nomAplicacio, String nomModul, String nomPermis, int idEmpleat) {
        var params = new HashMap<String, Object>();
        params.put("nom_aplicacio", nomAplicacio);
        params.put("nom_modul", nomModul);
        params.put("nom_permis", nomPermis);
        params.put("id_empleat", idEmpleat);
        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("organigrama_permisos")
                .withTableName("empleat_permis")
                .onConflictColumns("nom_aplicacio", "nom_modul", "nom_permis", "id_empleat")
                .execute(params);
    }

    @Override
    public void deleteEmpleat(String nomAplicacio, String nomModul, String nomPermis, int idEmpleat) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomModul", nomModul)
                .addValue("nomPermis", nomPermis)
                .addValue("idEmpleat", idEmpleat);

        jdbc.update("""
            DELETE FROM organigrama_permisos.empleat_permis
            WHERE nom_aplicacio = :nomAplicacio
              AND nom_modul = :nomModul
              AND nom_permis = :nomPermis
              AND id_empleat = :idEmpleat
            """, params);

    }

    @Override
    public List<Empleat> listAllEmpleatDelPermis(String nomAplicacio, String nomModul, String nomPermis) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomModul", nomModul)
                .addValue("nomPermis", nomPermis);

        return jdbc.query("""
                SELECT e.id, e.nom, e.cognoms, e.email, 'EMPLEAT' AS tipus_assignacio
                FROM organigrama_permisos.empleat_permis ep
                JOIN organigrama.empleats e
                    ON e.id = ep.id_empleat
                WHERE ep.nom_aplicacio = :nomAplicacio
                  AND ep.nom_modul = :nomModul
                  AND ep.nom_permis = :nomPermis
        
                UNION
        
                SELECT e.id, e.nom, e.cognoms, e.email, 'FUNCIO' AS tipus_assignacio
                FROM organigrama_permisos.funcio_permis fp
                JOIN organigrama.funcions_departament_centre fdc ON
                    (fp.id_centre IS NULL OR fp.id_centre = fdc.centre)
                    AND (fp.id_departament IS NULL OR fp.id_departament = fdc.departament)
                    AND (fp.id_funcio IS NULL OR fp.id_funcio = fdc.funcio)
                JOIN organigrama.empleat_funcio ef
                    ON ef.id_func_dep_cent = fdc.id
                JOIN organigrama.empleats e
                    ON e.id = ef.id_persona
                WHERE fp.nom_aplicacio = :nomAplicacio
                  AND fp.nom_modul = :nomModul
                  AND fp.nom_permis = :nomPermis
                ORDER BY id;
        """, params, new EmpleatAssignacioMapper());
    }
}

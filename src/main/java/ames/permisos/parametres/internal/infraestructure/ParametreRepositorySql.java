package ames.permisos.parametres.internal.infraestructure;

import ames.permisos.parametres.internal.domain.EmpleatParametre;
import ames.permisos.parametres.internal.domain.FuncioParametre;
import ames.permisos.parametres.internal.domain.Parametre;
import ames.permisos.parametres.internal.infraestructure.mapper.EmpleatParametreAssignacioMapper;
import ames.permisos.parametres.internal.infraestructure.mapper.EmpleatParametreMapper;
import ames.permisos.parametres.internal.infraestructure.mapper.FuncioParametreMapper;
import ames.permisos.parametres.internal.infraestructure.mapper.ParametreMapper;
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
public class ParametreRepositorySql implements ParametreRepository {

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
    public List<Parametre> obtenirParametresByAplicacio(String nomAplicacio) {
        return jdbcAmes.query(
                """
                        SELECT *
                        FROM organigrama_permisos.parametre
                        WHERE nom_aplicacio = ?;
                """, new ParametreMapper(), nomAplicacio
        );
    }

    @Override
    public Optional<Parametre> find(String nomAplicacio, String nomParametre) {
        return jdbcAmes.query("""
                SELECT nom_aplicacio, nom_parametre, descripcio
                FROM organigrama_permisos.parametre
                WHERE nom_aplicacio = ? AND nom_parametre = ?;
            """, new ParametreMapper(), nomAplicacio, nomParametre).stream().findFirst();
    }

    @Override
    public void save(String nomAplicacio, String nomParametre, String descripcio) {
        var params = new HashMap<String, Object>();
        params.put("nom_aplicacio", nomAplicacio);
        params.put("nom_parametre", nomParametre);
        params.put("descripcio", descripcio);
        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("organigrama_permisos")
                .withTableName("parametre")
                .onConflictColumns("nom_aplicacio", "nom_parametre")
                .execute(params);
    }

    @Override
    public void delete(String nomAplicacio, String nomParametre) {
        jdbcAmes.update("DELETE FROM organigrama_permisos.parametre WHERE nom_aplicacio = ? AND nom_parametre = ?", nomAplicacio, nomParametre);
    }

    @Override
    public List<FuncioParametre> listFuncioDelParametre(String nomAplicacio, String nomParametre) {
        return jdbcAmes.query("""
                SELECT fp.id_centre, c.nom AS nom_centre, fp.id_departament,
                        d.descripcio AS nom_departament, fp.id_funcio, f.descripcio AS nom_funcio,
                        fp.valor
                FROM organigrama_permisos.funcio_parametre fp
                LEFT JOIN organigrama.centres c ON c.id = fp.id_centre
                LEFT JOIN organigrama.departament d ON d.id = fp.id_departament
                LEFT JOIN organigrama.funcio f ON f.id = fp.id_funcio
                WHERE fp.nom_aplicacio = ? AND fp.nom_parametre = ?;
            """, new FuncioParametreMapper(json), nomAplicacio, nomParametre);
    }

    @Override
    public Optional<FuncioParametre> findAssignacioFuncio(String nomAplicacio, String nomParametre, FuncioParametre funcioParametre) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomParametre", nomParametre)
                .addValue("idCentre", funcioParametre.idCentre().orElse(null), Types.INTEGER)
                .addValue("idDepartament", funcioParametre.idDepartament().orElse(null), Types.INTEGER)
                .addValue("idFuncio", funcioParametre.idFuncio().orElse(null), Types.INTEGER);

        Optional<FuncioParametre> funcioAssignada = jdbc.query("""
            SELECT fp.*, c.nom AS nom_centre, fp.id_departament,
                d.descripcio AS nom_departament, fp.id_funcio, f.descripcio AS nom_funcio
            FROM organigrama_permisos.funcio_parametre fp
            LEFT JOIN organigrama.centres c ON c.id = fp.id_centre
            LEFT JOIN organigrama.departament d ON d.id = fp.id_departament
            LEFT JOIN organigrama.funcio f ON f.id = fp.id_funcio
            WHERE fp.nom_aplicacio = :nomAplicacio AND fp.nom_parametre = :nomParametre
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
        """, params, new FuncioParametreMapper(json)).stream().findFirst();

        return funcioAssignada;
    }

    @Override
    public void saveAssignacioFuncio(String nomAplicacio, String nomParametre, FuncioParametre funcioParametre) {
        var params = new HashMap<String, Object>();
        params.put("nom_aplicacio", nomAplicacio);
        params.put("nom_parametre", nomParametre);
        params.put("id_centre", funcioParametre.idCentre().orElse(null));
        params.put("id_departament", funcioParametre.idDepartament().orElse(null));
        params.put("id_funcio", funcioParametre.idFuncio().orElse(null));
        params.put("valor", funcioParametre.valor());
        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("organigrama_permisos")
                .withTableName("funcio_parametre")
                .onConflictColumns("nom_aplicacio", "nom_parametre", "id_centre", "id_departament", "id_funcio")
                .execute(params);
    }

    @Override
    public void deleteFuncio(String nomAplicacio, String nomParametre, FuncioParametre funcioParametre) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomParametre", nomParametre)
                .addValue("idCentre", funcioParametre.idCentre().orElse(null), Types.INTEGER)
                .addValue("idDepartament", funcioParametre.idDepartament().orElse(null), Types.INTEGER)
                .addValue("idFuncio", funcioParametre.idFuncio().orElse(null), Types.INTEGER);

        jdbc.update("""
            DELETE FROM organigrama_permisos.funcio_parametre
            WHERE nom_aplicacio = :nomAplicacio
              AND nom_parametre = :nomParametre
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
    public List<EmpleatParametre> listEmpleatDelParametre(String nomAplicacio, String nomParametre) {
        return jdbcAmes.query("""
                SELECT DISTINCT e.id, e.nom, e.cognoms, e.email, ep.valor
                FROM organigrama_permisos.empleat_parametre ep
                JOIN organigrama.empleats e ON e.id = ep.id_empleat
                WHERE ep.nom_aplicacio = ? AND ep.nom_parametre = ?;
            """, new EmpleatParametreMapper(), nomAplicacio, nomParametre);
    }

    @Override
    public Optional<EmpleatParametre> findAssignacioEmpleat(String nomAplicacio, String nomParametre, int idEmpleat) {
        return jdbcAmes.query("""
                SELECT ep.nom_aplicacio, ep.nom_parametre, e.id, e.nom, e.cognoms, e.email, ep.valor
                FROM organigrama_permisos.empleat_parametre ep
                JOIN organigrama.empleats e ON e.id = ep.id_empleat
                WHERE ep.nom_aplicacio = ? AND ep.nom_parametre = ? AND ep.id_empleat = ?;
            """, new EmpleatParametreMapper(), nomAplicacio, nomParametre, idEmpleat).stream().findFirst();
    }

    @Override
    public void saveAssignacioEmpleat(String nomAplicacio, String nomParametre, String valor, int idEmpleat) {
        var params = new HashMap<String, Object>();
        params.put("nom_aplicacio", nomAplicacio);
        params.put("nom_parametre", nomParametre);
        params.put("valor", valor);
        params.put("id_empleat", idEmpleat);
        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("organigrama_permisos")
                .withTableName("empleat_parametre")
                .onConflictColumns("nom_aplicacio", "nom_parametre", "id_empleat")
                .execute(params);
    }

    @Override
    public void deleteEmpleat(String nomAplicacio, String nomParametre, int idEmpleat) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomParametre", nomParametre)
                .addValue("idEmpleat", idEmpleat);

        jdbc.update("""
            DELETE FROM organigrama_permisos.empleat_parametre
            WHERE nom_aplicacio = :nomAplicacio
              AND nom_parametre = :nomParametre
              AND id_empleat = :idEmpleat
            """, params);

    }

    @Override
    public List<EmpleatParametre> listAllEmpleatDelParametre(String nomAplicacio, String nomParametre) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nomAplicacio", nomAplicacio)
                .addValue("nomParametre", nomParametre);

        return jdbc.query("""
                SELECT e.id, e.nom, e.cognoms, e.email, ep.valor, 'EMPLEAT' AS tipus_assignacio
                FROM organigrama_permisos.empleat_parametre ep
                JOIN organigrama.empleats e
                    ON e.id = ep.id_empleat
                WHERE ep.nom_aplicacio = :nomAplicacio
                  AND ep.nom_parametre = :nomParametre

                UNION

                SELECT e.id, e.nom, e.cognoms, e.email, fp.valor, 'FUNCIO' AS tipus_assignacio
                FROM organigrama_permisos.funcio_parametre fp
                JOIN organigrama.funcions_departament_centre fdc ON
                    (fp.id_centre IS NULL OR fp.id_centre = fdc.centre)
                    AND (fp.id_departament IS NULL OR fp.id_departament = fdc.departament)
                    AND (fp.id_funcio IS NULL OR fp.id_funcio = fdc.funcio)
                JOIN organigrama.empleat_funcio ef
                    ON ef.id_func_dep_cent = fdc.id
                JOIN organigrama.empleats e
                    ON e.id = ef.id_persona
                WHERE fp.nom_aplicacio = :nomAplicacio
                  AND fp.nom_parametre = :nomParametre
                ORDER BY id;
        """, params, new EmpleatParametreAssignacioMapper());
    }
}

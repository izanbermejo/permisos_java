package ames.permisos.organigrama.internal.infraestructure;

import ames.permisos.organigrama.internal.domain.Empleat;
import ames.permisos.organigrama.internal.infraestructure.mapper.EmpleatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.List;

@Repository
public class EmpleatRepositorySql implements EmpleatRepository {

    @Autowired NamedParameterJdbcTemplate jdbc;

    @Override
    public List<Empleat> obtenirEmpleatsByCentDepFun(Integer idCentre, Integer idDepartament, Integer idFuncio) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idCentre", idCentre, Types.INTEGER)
                .addValue("idDepartament", idDepartament, Types.INTEGER)
                .addValue("idFuncio", idFuncio, Types.INTEGER);

        List<Empleat> empleats = jdbc.query("""
            SELECT DISTINCT e.id, e.nom, e.cognoms, e.email
            FROM organigrama.funcions_departament_centre fdc
            JOIN organigrama.empleat_funcio ef ON ef.id_func_dep_cent = fdc.id
            JOIN organigrama.empleats e ON e.id = ef.id_persona
            WHERE
            ( :idCentre IS NULL OR fdc.centre = :idCentre )
            AND
            ( :idDepartament IS NULL OR fdc.departament = :idDepartament )
            AND
            ( :idFuncio IS NULL OR fdc.funcio = :idFuncio );
        """, params, new EmpleatMapper());

        return empleats;
    }
}

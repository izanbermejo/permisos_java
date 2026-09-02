package ames.permisos.organigrama.internal.infraestructure;

import ames.permisos.organigrama.internal.domain.Funcio;
import ames.permisos.organigrama.internal.infraestructure.mapper.FuncioMapper;
import ames.permisos.server.Json;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Repository
public class FuncioRepositorySql implements FuncioRepository {

    private @Autowired ObjectMapper jsonMapper;
    private Json json;
    private TypeReference<Map<String,String>> mapType;
    @Autowired NamedParameterJdbcTemplate jdbc;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
        this.mapType = new TypeReference<Map<String,String>>() {};
    }

    @Override
    public List<Funcio> obtenirFuncionsByEmpleat(int idEmpleat) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idEmpleat", idEmpleat);

        List<Funcio> funcions = jdbc.query("""
            SELECT fdc.centre as id_centre, fdc.departament as id_departament, fdc.funcio as id_funcio,
                c.nom as nom_centre, d.descripcio as nom_departament, f.descripcio as nom_funcio
            FROM organigrama.funcions_departament_centre fdc
            JOIN organigrama.empleat_funcio ef ON ef.id_func_dep_cent = fdc.id
            JOIN organigrama.empleats e ON e.id = ef.id_persona
            JOIN organigrama.centres c ON c.id = fdc.centre
            JOIN organigrama.departament d ON d.id = fdc.departament
            JOIN organigrama.funcio f ON f.id = fdc.funcio
            WHERE e.id = :idEmpleat;
        """, params, new FuncioMapper(json));

        return funcions;
    }
}

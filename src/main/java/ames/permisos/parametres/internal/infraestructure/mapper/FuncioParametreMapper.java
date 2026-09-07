package ames.permisos.parametres.internal.infraestructure.mapper;

import ames.permisos.parametres.internal.domain.FuncioParametre;
import ames.permisos.parametres.internal.domain.FuncioParametreImpl;
import ames.permisos.server.Json;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

public class FuncioParametreMapper implements RowMapper<FuncioParametre> {

    private Json json;
    private TypeReference<Map<String,String>> mapType;

    public FuncioParametreMapper(Json json) {
        this.json = json;
        this.mapType = new TypeReference<Map<String,String>>() {};

    }

    @Override
    public FuncioParametre mapRow(ResultSet rs, int rowNum) throws SQLException {
        return FuncioParametreImpl.builder()
                .idCentre(Optional.ofNullable((Number) rs.getObject("id_centre"))
                        .map(Number::intValue))
                .nomCentre(Optional.ofNullable(rs.getString("nom_centre")))
                .idDepartament(Optional.ofNullable((Number) rs.getObject("id_departament"))
                        .map(Number::intValue))
                .nomDepartament(Optional.ofNullable(rs.getString("nom_departament"))
                        .map(value -> json.deserialize(value, mapType)))
                .idFuncio(Optional.ofNullable((Number) rs.getObject("id_funcio"))
                        .map(Number::intValue))
                .nomFuncio(Optional.ofNullable(rs.getString("nom_funcio"))
                        .map(value -> json.deserialize(value, mapType)))
                .valor(rs.getString("valor"))
                .build();
    }

}
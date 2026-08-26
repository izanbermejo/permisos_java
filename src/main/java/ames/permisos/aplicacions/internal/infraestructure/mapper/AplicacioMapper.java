package ames.permisos.aplicacions.internal.infraestructure.mapper;

import ames.permisos.aplicacions.internal.domain.Aplicacio;
import ames.permisos.aplicacions.internal.domain.AplicacioImpl;
import org.springframework.jdbc.core.*;

import java.sql.*;

public class AplicacioMapper implements RowMapper<Aplicacio> {

    @Override
    public Aplicacio mapRow(ResultSet rs, int rowNum) throws SQLException {
        return AplicacioImpl.builder()
                .nomAplicacio(rs.getString("nom_aplicacio"))
                .descripcio(rs.getString("descripcio"))
                .build();
    }

}

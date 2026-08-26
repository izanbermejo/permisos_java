package ames.permisos.parametres.internal.infraestructure.mapper;

import ames.permisos.parametres.internal.domain.Parametre;
import ames.permisos.parametres.internal.domain.ParametreImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ParametreMapper implements RowMapper<Parametre> {

    @Override
    public Parametre mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ParametreImpl.builder()
                .nomAplicacio(rs.getString("nom_aplicacio"))
                .nomParametre(rs.getString("nom_parametre"))
                .descripcio(rs.getString("descripcio"))
                .build();
    }

}

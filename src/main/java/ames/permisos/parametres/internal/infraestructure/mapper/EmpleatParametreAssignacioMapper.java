package ames.permisos.parametres.internal.infraestructure.mapper;

import ames.permisos.parametres.internal.domain.EmpleatParametre;
import ames.permisos.parametres.internal.domain.EmpleatParametreImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class EmpleatParametreAssignacioMapper implements RowMapper<EmpleatParametre> {

    @Override
    public EmpleatParametre mapRow(ResultSet rs, int rowNum) throws SQLException {
        return EmpleatParametreImpl.builder()
                .id(rs.getInt("id"))
                .nom(rs.getString("nom"))
                .cognoms(rs.getString("cognoms"))
                .email(rs.getString("email"))
                .valor(rs.getString("valor"))
                .tipusAssignacio(Optional.ofNullable(rs.getString("tipus_assignacio")))
                .build();
    }

}

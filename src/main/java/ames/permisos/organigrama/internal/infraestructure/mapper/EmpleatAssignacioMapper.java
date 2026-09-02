package ames.permisos.organigrama.internal.infraestructure.mapper;

import ames.permisos.organigrama.internal.domain.Empleat;
import ames.permisos.organigrama.internal.domain.EmpleatImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class EmpleatAssignacioMapper implements RowMapper<Empleat> {

    @Override
    public Empleat mapRow(ResultSet rs, int rowNum) throws SQLException {
        return EmpleatImpl.builder()
                .id(rs.getInt("id"))
                .nom(rs.getString("nom"))
                .cognoms(rs.getString("cognoms"))
                .email(rs.getString("email"))
                .tipusAssignacio(Optional.ofNullable(rs.getString("tipus_assignacio")))
                .build();
    }

}

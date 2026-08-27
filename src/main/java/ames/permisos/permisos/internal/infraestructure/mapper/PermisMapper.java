package ames.permisos.permisos.internal.infraestructure.mapper;

import ames.permisos.permisos.internal.domain.Permis;
import ames.permisos.permisos.internal.domain.PermisImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PermisMapper implements RowMapper<Permis> {

    @Override
    public Permis mapRow(ResultSet rs, int rowNum) throws SQLException {
        return PermisImpl.builder()
                .nomAplicacio(rs.getString("nom_aplicacio"))
                .nomModul(rs.getString("nom_modul"))
                .nomPermis(rs.getString("nom_permis"))
                .descripcio(rs.getString("descripcio"))
                .build();
    }

}

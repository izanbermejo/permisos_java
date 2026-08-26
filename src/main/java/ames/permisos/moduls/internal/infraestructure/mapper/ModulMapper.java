package ames.permisos.moduls.internal.infraestructure.mapper;

import ames.permisos.moduls.internal.domain.Modul;
import ames.permisos.moduls.internal.domain.ModulImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ModulMapper implements RowMapper<Modul> {

    @Override
    public Modul mapRow(ResultSet rs, int rowNum) throws SQLException {
        return ModulImpl.builder()
                .nomAplicacio(rs.getString("nom_aplicacio"))
                .nomModul(rs.getString("nom_modul"))
                .descripcio(rs.getString("descripcio"))
                .build();
    }

}

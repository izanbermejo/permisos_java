package ames.permisos.shared.mapper;

import ames.permisos.server.Json;
import ames.permisos.shared.Usuari;
import ames.permisos.shared.UsuariImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuariMapper implements RowMapper<Usuari> {

	private Json json;

	public UsuariMapper(Json json) {
		this.json = json;
	}

	@Override
	public Usuari mapRow(ResultSet rs, int rowNum) throws SQLException {
			return UsuariImpl.builder()
					.nom(rs.getString("usuari"))
					.id(rs.getString("usuari"))
					.build();

	}

}

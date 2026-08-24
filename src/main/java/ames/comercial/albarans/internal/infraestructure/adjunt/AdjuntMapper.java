package ames.comercial.albarans.internal.infraestructure.adjunt;

import ames.comercial.albarans.internal.domain.Adjunt;
import ames.comercial.albarans.internal.domain.AdjuntImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AdjuntMapper implements RowMapper<Adjunt> {

	@Override
	public Adjunt mapRow(ResultSet rs, int rowNum) throws SQLException {
		return AdjuntImpl.builder()
				.albara(rs.getLong("albara"))
				.codiFitxer(rs.getString("codi"))
				.nom(rs.getString("nom_fitxer"))
				.usuari(rs.getString("usuari"))
				.data(rs.getTimestamp("data").toLocalDateTime())
				.build();
	}

}

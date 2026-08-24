package ames.comercial.clients.internal.infraestructure.adjunt;

import ames.comercial.clients.internal.domain.Adjunt;
import ames.comercial.clients.internal.domain.AdjuntImpl;
import ames.comercial.clients.internal.domain.Categoria;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AdjuntMapper implements RowMapper<Adjunt> {

	@Override
	public Adjunt mapRow(ResultSet rs, int rowNum) throws SQLException {
		return AdjuntImpl.builder()
				.client(rs.getString("client"))
				.codiFitxer(rs.getString("codi"))
				.nom(rs.getString("nom_fitxer"))
				.categoria(Categoria.getById(rs.getLong("categoria")))
				.usuari(rs.getString("usuari"))
				.data(rs.getTimestamp("data").toLocalDateTime())
				.build();
	}

}

package ames.comercial.tarifes.internal.infraestructure.mapper;

import ames.comercial.server.Json;
import ames.comercial.tarifes.internal.domain.DadesPreu;
import ames.comercial.tarifes.internal.domain.DadesPreuImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PreuRecordMapper implements RowMapper<PreuRecord> {

	private Json json;

	public PreuRecordMapper(Json json) {
		this.json = json;
	}

	@Override
	public PreuRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		// Dades de la comanda
		DadesPreu dades = DadesPreuImpl.builder()
//				.codi_tarifa(rs.getLong("codi_tarifa"))
				.artInt(rs.getString("artint"))
				.pr01(rs.getDouble("pr01"))
				.pr02(rs.getDouble("pr02"))
				.pr03(rs.getDouble("pr03"))
				.pr04(rs.getDouble("pr04"))
				.pr05(rs.getDouble("pr05"))
				.pr06(rs.getDouble("pr06"))
				.pr07(rs.getDouble("pr07"))
				.pr08(rs.getDouble("pr08"))
				.pr09(rs.getDouble("pr09"))
				.pr10(rs.getDouble("pr10"))
				.pr11(rs.getDouble("pr11"))
				.pr12(rs.getDouble("pr12"))
				.aclFab(rs.getString("aclfab"))
				.aclRef(rs.getString("aclref"))
				.unsBos(rs.getInt("unsbos"))
				.unsCai(rs.getInt("unscai"))
				.build();

		return PreuRecordImpl.builder()
				.dades(dades)
				.build();

	}

}

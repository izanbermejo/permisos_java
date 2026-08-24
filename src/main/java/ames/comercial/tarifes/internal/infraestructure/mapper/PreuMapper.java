package ames.comercial.tarifes.internal.infraestructure.mapper;

import ames.comercial.server.Json;
import ames.comercial.tarifes.beans.Preu;
import ames.comercial.tarifes.internal.domain.DadesPreuImpl;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PreuMapper implements RowMapper<Preu> {

	private Json json;

	public PreuMapper(Json json) {
		this.json = json;
	}

	@Override
	public Preu mapRow(ResultSet rs, int rowNum) throws SQLException {
		// Dades de la comanda
		Preu preu = new Preu();
		preu.setCodi_tarifa(rs.getLong("codi_tarifa"));
		preu.setArtInt(rs.getString("artint"));
		preu.setPr01(rs.getDouble("pr01"));
		preu.setPr02(rs.getDouble("pr02"));
		preu.setPr03(rs.getDouble("pr03"));
		preu.setPr04(rs.getDouble("pr04"));
		preu.setPr05(rs.getDouble("pr05"));
		preu.setPr06(rs.getDouble("pr06"));
		preu.setPr07(rs.getDouble("pr07"));
		preu.setPr08(rs.getDouble("pr08"));
		preu.setPr09(rs.getDouble("pr09"));
		preu.setPr10(rs.getDouble("pr10"));
		preu.setPr11(rs.getDouble("pr11"));
		preu.setPr12(rs.getDouble("pr12"));
		preu.setAclFab(rs.getString("aclfab"));
		preu.setAclRef(rs.getString("aclref"));
//		preu.setUnsBos(rs.getInt("unsbos"));
//		preu.setUnsCai(rs.getInt("unscai"));
		return preu;

	}

}

package ames.comercial.tarifes.internal.infraestructure;

import ames.comercial.server.Json;
import ames.comercial.tarifes.beans.Preu;
import ames.comercial.tarifes.internal.domain.DadesTarifa;
import ames.comercial.tarifes.internal.domain.DadesTarifaImpl;
import ames.comercial.tarifes.internal.infraestructure.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Repository
public class TarifaRepositorySQL implements TarifaRepository {
	
	private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
	private @Autowired ObjectMapper jsonMapper;
	private Json json;

	private String schema = "tarifes_normalitzats";
	
	@PostConstruct
	public void init () {
		json = new Json(jsonMapper);
	}

	@Override
	public long nextId() {
		return jdbcAmes.queryForObject("SELECT nextval('"+schema+".seq_tarifa')", Long.class);
	}

	public long nextPreuId() {
		return jdbcAmes.queryForObject("SELECT nextval('"+schema+".seq_preu')", Long.class);
	}

	@Override
	public Long save(DadesTarifa dadesTarifa) {
		Long codi = nextId();
		var params = new HashMap<String, Object>();

		params.put("codi", codi);
		params.put("nom", dadesTarifa.nom());
		params.put("divisa", dadesTarifa.divisa());
		params.put("tram1",dadesTarifa.tram01());
		params.put("tram2",dadesTarifa.tram02());
		params.put("tram3",dadesTarifa.tram03());
		params.put("tram4",dadesTarifa.tram04());
		params.put("tram5",dadesTarifa.tram05());
		params.put("tram6",dadesTarifa.tram06());
		params.put("tram7",dadesTarifa.tram07());
		params.put("tram8",dadesTarifa.tram08());
		params.put("tram9",dadesTarifa.tram09());
		params.put("tram10",dadesTarifa.tram12());
		params.put("tram11",dadesTarifa.tram11());
		params.put("tram12",dadesTarifa.tram12());
		params.put("inserted_at", dadesTarifa.insertedAt());
		params.put("inserted_by", dadesTarifa.insertedBy());
		params.put("updatedAt", dadesTarifa.updatedAt());
		params.put("updatedBy", dadesTarifa.updatedBy());
		params.put("enabled", dadesTarifa.enabled());
		params.put("status", dadesTarifa.status());
		if (!dadesTarifa.vinculada().isEmpty())
			params.put("vinculada", dadesTarifa.vinculada().get());
		params.put("deleted", dadesTarifa.enabled());

		new SimpleJdbcInsert(jdbcAmes)
			.withSchemaName(schema)
			.withTableName("tarifa")
			.execute(params);

		return codi;
	}

//	@Override
//	public int updateTarifaField(String tarifa, String fieldName, Object value) {
//		String sql = "UPDATE tarifes_normalitzats.tarifa SET " + fieldName + " = ?  WHERE tarifa = ?";
//		return jdbcAmes.update(sql, value, tarifa);
//	}

	@Override
	public int updateTarifaFields(Long codi, HashMap<String, Object> fields) {
		StringJoiner setClause = new StringJoiner(", ");
		fields.forEach((key, value) -> setClause.add(key + " = ?"));

		String sql = "UPDATE "+schema+".tarifa SET " + setClause.toString() + ", updated_at = ? WHERE codi = ?";

		Object[] params = fields.values().toArray(new Object[fields.size() + 2]);
		params[fields.size()] = LocalDateTime.now();
		params[fields.size() + 1] = codi;

		return jdbcAmes.update(sql, params);
	}

//	@Override
//	public void  update(DadesTarifa dadesTarifa) {
//		String sql = "UPDATE "+schema+".tarifa SET " + fieldName + " = ? WHERE tarifa = ?";
//		jdbcAmes.update(sql, value, tarifa);
//	}

	@Override
	public List<DadesTarifa> findTarifes(String nom,String divisa,Integer any,String estat) {
		StringBuilder  sql = new StringBuilder("SELECT * FROM "+schema+".tarifa WHERE 1=1 AND enabled = true");
		List<Object> params = new ArrayList<>();

		if (StringUtils.hasText(nom)) {
			sql.append(" AND nom = ?");
			params.add(nom);
		}

		if (StringUtils.hasText(divisa)) {
			sql.append(" AND divisa = ?");
			params.add(divisa);
		}

		if (any!=null) {
			LocalDateTime primerDiaDelAny = getFirstDayOfYear(any);
			LocalDateTime ultimDiaDelAny = getLastDayOfYear(any);
			sql.append(" AND inserted_at between ? AND ?");
			params.add(primerDiaDelAny);
			params.add(ultimDiaDelAny);
		}

		if (StringUtils.hasText(estat)) {
			sql.append(" AND status = ?");
			params.add(estat);
		}
		sql.append(" ORDER BY updated_at DESC");
		return jdbcAmes.query(sql.toString(), params.toArray(),new TarifaMapper(json));
	}

	private static LocalDateTime getFirstDayOfYear(int year) {
		// Crear una instancia de LocalDate para el primer día del año especificado
		LocalDate firstDayOfYear = LocalDate.of(year, 1, 1);
		// Convertir LocalDate a LocalDateTime al inicio del día
		return firstDayOfYear.atStartOfDay();
	}


	private static LocalDateTime getLastDayOfYear(int year) {
		// Crear una instancia de LocalDate para el primer día del año especificado
		LocalDate firstDayOfYear = LocalDate.of(year, 12, 31);
		// Convertir LocalDate a LocalDateTime al inicio del día
		return firstDayOfYear.atTime(LocalTime.MAX);
	}

	@Override
	public Optional<DadesTarifa> find(Long codi) {
		try {
			var registreTarifa = jdbcAmes.queryForObject("SELECT * FROM "+schema+".tarifa WHERE codi = ?", new TarifaMapper(json), codi);
			var props = DadesTarifaImpl.builder()
					.nom(registreTarifa.nom())
					.divisa(registreTarifa.divisa())
					.insertedAt(registreTarifa.insertedAt())
					.insertedBy(registreTarifa.insertedBy())
//					.updatedAt(LocalDateTime.now())
//					.updatedBy("")
					.enabled(registreTarifa.enabled())
					.deleted(registreTarifa.deleted())
					.status(registreTarifa.status())
					.build();
//					.dades(rowComanda.dades())
//					.build();

			return Optional.of(props);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<DadesTarifa> findByNom(String nom) {
		try {
			var registreTarifa = jdbcAmes.queryForObject("SELECT * FROM "+schema+".tarifa WHERE nom = ?", new TarifaMapper(json), nom);
			var props = DadesTarifaImpl.builder()
					.codi(registreTarifa.codi())
					.nom(registreTarifa.nom())
					.divisa(registreTarifa.divisa())
					.insertedAt(registreTarifa.insertedAt())
					.insertedBy(registreTarifa.insertedBy())
//					.updatedAt(LocalDateTime.now())
//					.updatedBy("")
					.enabled(registreTarifa.enabled())
					.deleted(registreTarifa.deleted())
					.status(registreTarifa.status())
					.build();
//					.dades(rowComanda.dades())
//					.build();

			return Optional.of(props);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	@Override
	public Optional<DadesTarifa> findByCodi(Long codi) {
		try {
			var registreTarifa = jdbcAmes.queryForObject("SELECT * FROM "+schema+".tarifa WHERE codi = ?", new TarifaMapper(json), codi);
			var props = DadesTarifaImpl.builder()
					.codi(registreTarifa.codi())
					.nom(registreTarifa.nom())
					.divisa(registreTarifa.divisa())
					.insertedAt(registreTarifa.insertedAt())
					.insertedBy(registreTarifa.insertedBy())
					.enabled(registreTarifa.enabled())
					.deleted(registreTarifa.deleted())
					.status(registreTarifa.status())
					.tram01(registreTarifa.tram01())
					.tram02(registreTarifa.tram02())
					.tram03(registreTarifa.tram03())
					.tram04(registreTarifa.tram04())
					.tram05(registreTarifa.tram05())
					.tram06(registreTarifa.tram06())
					.tram07(registreTarifa.tram07())
					.tram08(registreTarifa.tram08())
					.tram09(registreTarifa.tram09())
					.tram10(registreTarifa.tram10())
					.tram11(registreTarifa.tram11())
					.tram12(registreTarifa.tram12())
					.build();

			return Optional.of(props);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}
	@Override
	public boolean exists(Long codi) {
		try {
			var registreTarifa = jdbcAmes.queryForObject("SELECT count(*) FROM "+schema+".tarifa WHERE codi = ?", Integer.class, codi);
			return (registreTarifa>0);
		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	@Override
	public boolean exists(String nom) {
		try {
			var registreTarifa = jdbcAmes.queryForObject("SELECT count(*) FROM "+schema+".tarifa WHERE nom = ?", Integer.class, nom);
			return (registreTarifa>0);
		} catch (EmptyResultDataAccessException e) {
			return false;
		}
	}

	@Override
	public List<PreuRecord> findPreusRecord(Long codi_tarifa) {
		String sql = "SELECT codi_tarifa,artint, pr01, pr02, pr03, pr04, pr05, pr06, pr07, pr08, pr09, pr10, pr11, pr12, aclfab, aclref " +
				"FROM "+schema+".preu WHERE codi_tarifa = ?";

		return jdbcAmes.query(sql, new PreuRecordMapper(json),codi_tarifa);
	}

	@Override
	public List<Preu> findPreus(Long codi_tarifa) {
		String sql = "SELECT codi_tarifa,artint, pr01, pr02, pr03, pr04, pr05, pr06, pr07, pr08, pr09, pr10, pr11, pr12, aclfab, aclref " +
				"FROM "+schema+".preu WHERE codi_tarifa = ?";

		return jdbcAmes.query(sql, new PreuMapper(json),codi_tarifa);
	}


	@Override
	public void savePreus(Long codi_tarifa,List<Preu> preus) {

		String sql = "INSERT INTO "+schema+".preu (codi_tarifa, artint, pr01, pr02, pr03, pr04, pr05, pr06, pr07, pr08, pr09, pr10, pr11, pr12, aclfab, aclref) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

		jdbcAmes.batchUpdate(sql, new org.springframework.jdbc.core.BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Preu preu = preus.get(i);
				ps.setLong(1, codi_tarifa);
				ps.setString(2, preu.getArtInt());
				ps.setDouble(3, preu.getPr01());
				ps.setDouble(4, preu.getPr02());
				ps.setDouble(5, preu.getPr03());
				ps.setDouble(6, preu.getPr04());
				ps.setDouble(7, preu.getPr05());
				ps.setDouble(8, preu.getPr06());
				ps.setDouble(9, preu.getPr07());
				ps.setDouble(10, preu.getPr08());
				ps.setDouble(11, preu.getPr09());
				ps.setDouble(12, preu.getPr10());
				ps.setDouble(13, preu.getPr11());
				ps.setDouble(14, preu.getPr12());
				ps.setString(15, preu.getAclFab());
				ps.setString(16, preu.getAclRef());
//				ps.setInt(17, preu.getUnsBos());
//				ps.setInt(18, preu.getUnsCai());
			}

			@Override
			public int getBatchSize() {
				return preus.size();
			}
		});
	}

	public int delete(Long codi) {
		return jdbcAmes.update("DELETE FROM "+schema+".tarifa WHERE codi = ?", codi);
	}

	public int deletePreus(Long codi_tarifa) {
		return jdbcAmes.update("DELETE FROM "+schema+".preu WHERE codi_tarifa = ?", codi_tarifa);
	}

//	private void remove (Optional<Long> codi) {
//		jdbcAmes.update("DELETE FROM "+schema+".tarifa WHERE codi = ?", codi);
//	}

//	@Override
//	public void checkEtag (long codi) {
//		var expectedVersion = RequestThread.etag(Entity.COMANDA);
//		if (!expectedVersion.isEmpty()) {
//			etag(codi).ifPresent(etag -> {
//				if (!expectedVersion.equals(etag.versio()))
//					throw new ComandaChanged(etag);
//			});
//		}
//	}

//	@Override
//	public Optional<ETag> etag (long codi) {
//		try {
//			var type = new TypeReference<ETag>() {};
//			var currentETag = jdbcAmes.queryForObject(
//				"SELECT etag FROM comandes.comanda WHERE codi = ?",
//				(rs, rowNum) -> json.deserialize(rs.getString(1), type),
//				codi
//			);
//			return Optional.of(currentETag);
//		} catch (EmptyResultDataAccessException empty) {
//			return Optional.empty();
//		}
//	}
	
}

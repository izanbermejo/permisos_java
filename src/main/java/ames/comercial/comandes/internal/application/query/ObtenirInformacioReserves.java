package ames.comercial.comandes.internal.application.query;

import ames.comercial.comandes.internal.domain.comanda.Servible;
import ames.comercial.comandes.internal.domain.linia.Reservable;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ObtenirInformacioReserves {

	@Autowired
	NamedParameterJdbcTemplate jdbc;

	public ObtenirInformacioReservesResp executar (String artint, String empresa) {
        return ObtenirInformacioReservesRespImpl.builder()
				.reserves(queryReserves(artint, empresa))
				.cua(queryCuaReserves(artint, empresa))
				.build();
    }

	private List<LiniaReserva> queryReserves (String artint, String empresa) {
		var params = new MapSqlParameterSource("artint", artint);
		params.addValue("empresa", empresa);
		return jdbc.query("""
				SELECT c.codi, c.client, c.client_nom, c.servible, lc.estat_reserva, lc.numero, lc.data_solicitada, lc.quantitat_reservada, lc.quantitat, lc.quantitat_servida
				FROM comandes.linia_comanda lc
				LEFT JOIN comandes.comanda c ON lc.comanda = c.codi
				WHERE lc.quantitat_reservada > 0 AND artint = :artint AND actual
					AND c.empresa = :empresa
				ORDER BY lc.data_solicitada ASC, c.codi ASC, lc.numero ASC;
				""", params, new LiniaReservaMapper());
	}

	private List<LiniaReserva> queryCuaReserves (String artint, String empresa) {
		var params = new MapSqlParameterSource("artint", artint);
		params.addValue("empresa", empresa);
		return jdbc.query("""
				SELECT c.codi, c.client, c.client_nom, c.servible, lc.estat_reserva, lc.numero, lc.data_solicitada, lc.quantitat_reservada, lc.quantitat, lc.quantitat_servida
				FROM comandes.linia_comanda lc
				LEFT JOIN comandes.comanda c ON lc.comanda = c.codi
				WHERE lc.clicod = '000000' AND lc.quantitat_reservada < quantitat_pendent AND artint = :artint
					AND actual AND c.empresa = :empresa
				ORDER BY lc.data_solicitada ASC, c.codi ASC, lc.numero ASC;
				""", params, new LiniaReservaMapper());
	}

	@JsonDeserialize(builder = ObtenirInformacioReservesRespImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	public interface ObtenirInformacioReservesResp {
		List<LiniaReserva> reserves();
		List<LiniaReserva> cua();
	}

	@JsonDeserialize(builder = LiniaReservaImpl.Builder.class)
	@Value.Style(typeImmutable = "*Impl")
	@Value.Immutable
	public interface LiniaReserva {
		long comanda();
		long numero();
		Servible servible();
		Reservable reservable();
		String clientCodi();
		String clientDesc();
		LocalDate dataSolicitada();
		long quantitatReservada();
		long quantitat();
		long quantitatServida();
		@Derived default long quantitatPendent() { return quantitat() - quantitatServida(); }
		@Derived default String codiFormat() { return String.format("%07d", comanda()); }
		@Derived default String numeroFormat() {
			return String.format("%04d", numero());
		}
		@Derived default String codiNumeroFormat() { return codiFormat() + " / " + numeroFormat(); }
		@Derived default String client() { return clientCodi() + " - " + clientDesc(); }
	}

	private static class LiniaReservaMapper implements RowMapper<LiniaReserva> {
		@Override
		public LiniaReserva mapRow(ResultSet rs, int rowNum) throws SQLException {
			return LiniaReservaImpl.builder()
					.comanda(rs.getLong("codi"))
					.numero(rs.getLong("numero"))
					.servible(Servible.valueOf(rs.getString("servible")))
					.reservable(Reservable.valueOf(rs.getString("estat_reserva")))
					.clientCodi(rs.getString("client"))
					.clientDesc(rs.getString("client_nom"))
					.dataSolicitada(rs.getDate("data_solicitada").toLocalDate())
					.quantitatReservada(rs.getLong("quantitat_reservada"))
					.quantitat(rs.getLong("quantitat"))
					.quantitatServida(rs.getLong("quantitat_servida"))
					.build();
		}
	}

}

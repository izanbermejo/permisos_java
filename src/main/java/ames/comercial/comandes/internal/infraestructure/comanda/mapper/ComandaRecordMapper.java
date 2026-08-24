package ames.comercial.comandes.internal.infraestructure.comanda.mapper;

import ames.comercial.comandes.internal.domain.comanda.*;
import ames.comercial.server.Json;
import ames.comercial.shared.Adresa;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.InformacioEnviament;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ComandaRecordMapper implements RowMapper<ComandaRecord> {
	
	private Json json;
	
	public ComandaRecordMapper (Json json) {
		this.json = json;
	}

	@Override
	public ComandaRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
		// Tipus comanda
		TipusComanda tipus = TipusComanda.valueOf(rs.getString("tipus"));
		
		// Dades de la comanda
		DadesComanda dades = DadesComandaImpl.builder()
				.client(rs.getString("client"))
				.clientNom(rs.getString("client_nom"))
				.empresa(rs.getString("empresa"))
				.dataAlta(rs.getDate("data_alta").toLocalDate())
				.build();
		
		// Informació del client
		InformacioClient infoClient = json.deserialize(rs.getString("informacio_client"), InformacioClient.class);

		// Adreça comanda
		Adresa adresa = json.deserialize(rs.getString("adresa"), Adresa.class);

		// Informació enviament
		InformacioEnviament informacioEnviament = json.deserialize(rs.getString("informacio_enviament"), InformacioEnviament.class);

		// Dades enviament justificant
		DadesEnviamentJustificant dadesEnviamentJustificant = json.deserialize(rs.getString("dades_enviament_justificant"), DadesEnviamentJustificant.class);
		
		// Informació de normalitzat (només per a les normalitzades i mixtes)
		Optional<DadesNormalitzat> dadesNormalitzat = TipusComanda.PROGRAMA == tipus
				? Optional.empty()
				: Optional.of(DadesNormalitzatImpl.builder()
						.importNet(rs.getBigDecimal("import_net"))
						.importBrut(rs.getBigDecimal("import_brut"))
						.divisa(Divisa.getBySymbol(rs.getString("divisa")))
						.pes(rs.getBigDecimal("pes"))
						.tarifes(json.deserialize(rs.getString("tarifes"), DadesNormalitzatTarifes.class))
						.costTransport(rs.getBigDecimal("cost_transport"))
						.build());
		
		return ComandaRecordImpl.builder()
				.codi(rs.getLong("codi"))
				.tipus(TipusComanda.valueOf(rs.getString("tipus")))
				.dades(dades)
				.informacioClient(infoClient)
				.adresa(adresa)
				.informacioEnviament(informacioEnviament)
				.dadesNormalitzat(dadesNormalitzat)
				.dadesEnviamentJustificant(Optional.ofNullable(dadesEnviamentJustificant))
				.stockSeguretat(rs.getBoolean("stock_seguretat"))
				.numAdjunts(rs.getInt("num_adjunts"))
				.servida(rs.getBoolean("servida"))
				.servible(Servible.valueOf(rs.getString("servible")))
				.usuari(rs.getString("usuari"))
				.build();
		
	}

}

package ames.comercial.comandes.internal.infraestructure.query.extractor;

import ames.comercial.comandes.internal.domain.comanda.InformacioClient;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.comandes.response.ItemLiniaComanda;
import ames.comercial.comandes.response.ItemLiniaComandaImpl;
import ames.comercial.server.Json;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.Preu;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemLiniaComandaExtractor implements ResultSetExtractor<List<ItemLiniaComanda>> { 
	
	private Json json;
	
	public ItemLiniaComandaExtractor (Json json) {
		this.json = json;
	}

	@Override
	public List<ItemLiniaComanda> extractData(ResultSet rs) throws SQLException, DataAccessException {
		var acumulat = 0;
		List<ItemLiniaComanda> resultat = new ArrayList<ItemLiniaComanda>();
		while (rs.next()) {
			// Quantitat pendent (s'acumula)
			var pendent = rs.getLong("quantitat_pendent");
			acumulat += pendent;
			// Comanda del client
			InformacioClient infoClient = json.deserialize(rs.getString("informacio_client"), InformacioClient.class);
			resultat.add(ItemLiniaComandaImpl.builder()
					.codi(rs.getLong("comanda"))
					.numero(rs.getLong("numero"))
					.empresa(rs.getString("empresa"))
					.tipus(TipusLiniaComanda.valueOf(rs.getString("tipus")))
					.comandaClient(infoClient.identificador())
					.programa(infoClient.programa())
					.dataSolicitada(rs.getDate("data_solicitada").toLocalDate())
					.dataPrevistaSortida(rs.getDate("data_prevista_sortida").toLocalDate())
					.dataPrevistaSortidaInterna(MapperUtils.readOptionalDate(rs, "data_prevista_sortida_interna"))
					.dataConfirmadaFabrica(MapperUtils.readOptionalDate(rs, "data_confirmada_fabrica"))
					.quantitat(rs.getLong("quantitat"))
					.quantitatServida(rs.getLong("quantitat_servida"))
					.preu(Preu.of(rs.getBigDecimal("preu"), rs.getString("divisa")))
					.isPreuFixat(rs.getBoolean("preu_fixat"))
					.quantitatPendent(rs.getLong("quantitat_pendent"))
					.versio(rs.getString("versio"))
					.quantitatAcumulada(acumulat)
					.comentarisInterns(Optional.ofNullable(rs.getString("comIntern")))
					.comentarisClient(Optional.ofNullable(rs.getString("comClient")))
					.comandaBlanca(MapperUtils.readOptionalLong(rs, "comanda_blanca"))
					.numAdjunts(rs.getLong("num_adjunts"))
					.build());
		}
		return resultat;
	}
	
}

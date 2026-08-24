package ames.comercial.comandes.internal.infraestructure.query.extractor;

import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.comandes.response.ItemHistoriaLiniaComanda;
import ames.comercial.comandes.response.ItemHistoriaLiniaComandaImpl;
import ames.comercial.server.MapperUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemHistoriaLiniaComandaMapper implements RowMapper<ItemHistoriaLiniaComanda> { 

	@Override
	public ItemHistoriaLiniaComanda mapRow(ResultSet rs, int rowNum) throws SQLException {
		return ItemHistoriaLiniaComandaImpl.builder()
				.comanda(rs.getLong("comanda"))
				.numero(rs.getLong("numero"))
				.tipus(TipusLiniaComanda.valueOf(rs.getString("tipus")))
				.quantitat(rs.getLong("quantitat"))
				.preu(rs.getBigDecimal("preu"))
				.divisa(rs.getString("divisa"))
				.isPreuFixat(rs.getBoolean("preu_fixat"))
				.dataSolicitada(rs.getDate("data_solicitada").toLocalDate())
				.dataPrevistaSortida(rs.getDate("data_prevista_sortida").toLocalDate())
				.dataPrevistaSortidaInterna(MapperUtils.readOptionalDate(rs, "data_prevista_sortida_interna"))
				.dataConfirmadaFabrica(MapperUtils.readOptionalDate(rs, "data_confirmada_fabrica"))
				.quantitatServida(rs.getLong("quantitat_servida"))
				.comandaBlanca(MapperUtils.readOptionalLong(rs, "comanda_blanca"))
				.usuari(rs.getString("usuari"))
				.datareg(rs.getTimestamp("datareg").toLocalDateTime())
				.build();
	}
	
}

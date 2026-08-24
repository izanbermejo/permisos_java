package ames.comercial.comandes.internal.infraestructure.liniacomanda.mapper;

import ames.comercial.comandes.internal.domain.linia.*;
import ames.comercial.server.Json;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import ames.comercial.shared.TipusArticleClient;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class LiniaComandaMapper implements RowMapper<LiniaComanda> {
	
	private Json json;
	
	public LiniaComandaMapper (Json json) {
		this.json = json;
	}

	@Override
	public LiniaComanda mapRow(ResultSet rs, int rowNum) throws SQLException {
		var estatReserva = rs.getString("estat_reserva");
		return LiniaComandaImpl.builder()
				.id(KeyLiniaComanda.of(rs.getLong("comanda"), rs.getLong("numero")))
				.articleClient(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
				.referencia(rs.getString("referencia"))
				.tipusArticleClient(TipusArticleClient.valueOf(rs.getString("tipus_article_client")))
				.tipus(TipusLiniaComanda.valueOf(rs.getString("tipus")))
				.quantitat(rs.getLong("quantitat"))
				.preu(Preu.of(rs.getBigDecimal("preu"), Divisa.getBySymbol(rs.getString("divisa"))))
				.isPreuFixat(rs.getBoolean("preu_fixat"))
				.dataCreacio(rs.getTimestamp("data_creacio") .toLocalDateTime())
				.dataSolicitada(rs.getDate("data_solicitada").toLocalDate())
				.dataPrevistaSortida(rs.getDate("data_prevista_sortida").toLocalDate())
				.dataPrevistaSortidaInterna(MapperUtils.readOptionalDate(rs, "data_prevista_sortida_interna"))
				.dataConfirmadaFabrica(MapperUtils.readOptionalDate(rs, "data_confirmada_fabrica"))
				.quantitatServida(rs.getLong("quantitat_servida"))
				.reserva(estatReserva == null
						? Optional.empty()
						: Optional.of(InformacioReservaImpl.builder()
						.estat(Reservable.valueOf(rs.getString("estat_reserva")))
						.quantitat(rs.getLong("quantitat_reservada"))
						.build()))
				.dadesCalcul(dadesCalcul(rs.getString("dades_calcul")))
				.comandaBlanca(MapperUtils.readOptionalLong(rs, "comanda_blanca"))
				.build();
	}

	public Optional<DadesCalculNormalitzat> dadesCalcul(String value) {
		if (value==null || value.isBlank())
			return Optional.empty();
		return Optional.of(json.deserialize(value, DadesCalculNormalitzat.class));
	}
	
}

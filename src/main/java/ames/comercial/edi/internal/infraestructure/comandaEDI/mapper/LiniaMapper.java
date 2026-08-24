package ames.comercial.edi.internal.infraestructure.comandaEDI.mapper;

import ames.comercial.edi.internal.domain.DadesLiniaEDI;
import ames.comercial.edi.internal.domain.DadesLiniaEDIImpl;
import ames.comercial.server.I18N;
import ames.comercial.server.Json;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LiniaMapper implements RowMapper<DadesLiniaEDI> {

	private Json json;

	public LiniaMapper(Json json) {
		this.json = json;
	}

	@Override
	public DadesLiniaEDI mapRow(ResultSet rs, int rowNum) throws SQLException {
		// Dades de la comanda
		DadesLiniaEDIImpl.Builder builder = DadesLiniaEDIImpl.builder();
				builder.codi(rs.getLong("codi"))
						.codi_comanda(rs.getLong("codi_comanda"))
				.quantitat(rs.getInt("quantitat"));
				if (rs.getDate("data_inicial")!=null)
					builder.dataInicial(rs.getDate("data_inicial"));
				if (rs.getDate("data_final")!=null)
					builder.dataFinal(rs.getDate("data_final"));
				if (rs.getString("observacions")!=null) {
					if (!rs.getString("observacions").equals(""))
						builder.observacions(rs.getString("observacions"));
//						builder.observacions(I18N.getLiteral(rs.getString("observacions")));
					else
						builder.observacions("");
				}
				builder.codiArticle(rs.getString("codi_article"))
				.codiArticleAmes(rs.getString("codi_article_ames"))
						.tipus(rs.getString("tipus"))
						.status(rs.getString("status"))
				.insertedBy(rs.getString("inserted_by"))
				.insertedAt(rs.getTimestamp("inserted_at").toLocalDateTime());

		if (rs.getTimestamp("updated_at") != null) {
			builder.updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
					.updatedBy(rs.getString("updated_by"))
					.updatedReason(rs.getString("updated_reason"));
		}

		if (rs.getTimestamp("deleted_at") != null) {
			builder.updatedAt(rs.getTimestamp("deleted_at").toLocalDateTime())
					.updatedBy(rs.getString("deleted_by"))
					.updatedReason(rs.getString("deleted_reason"));
		}
		builder.ultimAlbara(rs.getString("ultim_albara"));
		if (rs.getString("codi_linia_client")!=null)
			builder.codiLiniaClient(rs.getString("codi_linia_client"));
		if (rs.getString("codi_comanda_client")!=null)
			builder.codiComandaClient(rs.getString("codi_comanda_client"));
		builder.codiArticleFab(rs.getString("codi_article_fab"))
				.acumulatArticle(rs.getInt("acum_article"));

		return builder.build();

	}

}


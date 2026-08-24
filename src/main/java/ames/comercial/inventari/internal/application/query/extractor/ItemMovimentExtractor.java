package ames.comercial.inventari.internal.application.query.extractor;

import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.inventari.internal.application.query.ItemMovimentImpl;
import ames.comercial.inventari.internal.application.query.ObtenirMoviments.ItemMoviment;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import ames.comercial.server.Json;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import ames.comercial.shared.InformacioEnviament;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemMovimentExtractor implements ResultSetExtractor<List<ItemMoviment>> {

	private long stockActual;
	private LocalDate dataFi;
	private ClientAds client;
	private Json json;

	public ItemMovimentExtractor(long stockActual, LocalDate dataFi, ClientAds client, Json json) {
		this.stockActual = stockActual;
		this.dataFi = dataFi;
		this.client = client;
		this.json = json;
	}

	@Override
	public List<ItemMoviment> extractData(ResultSet rs) throws SQLException, DataAccessException {
		var existencia = stockActual;
		List<ItemMoviment> resultat = new ArrayList<ItemMoviment>();
		while (rs.next()) {
			var data = rs.getDate("data").toLocalDate();
			var tipus = TipusMoviment.valueOf(rs.getString("tipus"));
			var quantitat = rs.getLong("quantitat");
			if (!data.isAfter(dataFi)) {
				var informacioEnviament = resolInformacioEnviament(rs);
				resultat.add(ItemMovimentImpl.builder()
						.id(rs.getLong("id"))
						.data(data)
						.tipus(tipus)
						.quantitat(quantitat)
						.existencia(existencia)
						.numeroAlbara(readOptionalLong(rs, "linia_albara_numero"))
						.sortidaClientCodi(Optional.ofNullable(rs.getString("sortida_client")))
						.sortidaClientNom(Optional.ofNullable(rs.getString("sortida_client_nom")))
						.entradaFabrica(Optional.ofNullable(rs.getString("entrada_fabrica")))
						.traspasClientReceptor(Optional.ofNullable(rs.getString("traspas_client_receptor")))
						.traspasMagatzemReceptor(Optional.ofNullable(rs.getString("traspas_magatzem_receptor")))
						.traspasEmpresaReceptora(Optional.ofNullable(rs.getString("traspas_empresa_receptora")))
						.preu(readOptionalBigDecimal(rs, "preu"))
						.divisa(readOptionalDivisa(rs, "divisa"))
						.identificadorConsum(Optional.ofNullable(rs.getString("identificador_consum")))
						.albaraEspecial(Optional.ofNullable(rs.getString("albara_especial")))
						.tipusAlbara(Optional.ofNullable(rs.getString("tipus_albara")).map(TipusAlbara::valueOf))
						.observacions(Optional.ofNullable(rs.getString("observacions")))
						.isEnPreparacio(rs.getBoolean("is_en_preparacio"))
						.isServit(rs.getBoolean("is_servit"))
						.isEntregat(rs.getBoolean("is_entregat"))
						.isFacturat(rs.getBoolean("is_facturat"))
						.formaEnviament(informacioEnviament != null ? Optional.of(informacioEnviament.formaEnviament()) : Optional.empty())
						.incoterm(informacioEnviament != null ? Optional.of(informacioEnviament.incoterm()) : Optional.empty())
						.desti(informacioEnviament != null ? Optional.ofNullable(informacioEnviament.desti()) : Optional.empty())
						.transportista(informacioEnviament != null ? informacioEnviament.transportista() : Optional.empty())
						.isMateixaFormaEnviamentHabitual(isMateixaFormaEnviamentHabitual(informacioEnviament))
						.build()
				);
			}
			// El recorregut és descendent (del moviment més recent al més antic) i partim de
			// l'estoc actual de la fitxa. Per tant, a cada pas no s'aplica el moviment sinó que
			// es desfà, i l'efecte que tindria sobre la fitxa s'ha de restar en comptes de sumar.
			existencia -= tipus.quantitatCalculFitxa(quantitat);
		}
		return resultat;
	}

	private InformacioEnviament resolInformacioEnviament(ResultSet rs) throws SQLException {
		if (json == null) return null;
		String raw = rs.getString("informacio_enviament");
		if (raw == null) return null;
		return json.deserialize(raw, InformacioEnviament.class);
	}

	private boolean isMateixaFormaEnviamentHabitual(InformacioEnviament informacioEnviament) {
		if (informacioEnviament == null || client == null) return true;
		return client.formaEnviament().equals(informacioEnviament.formaEnviament()) &&
				client.incoterm().equals(informacioEnviament.incoterm()) &&
				client.desti().equals(informacioEnviament.desti());
	}

	private Optional<Long> readOptionalLong(ResultSet rs, String column) throws SQLException {
		long val = rs.getLong(column);
		return rs.wasNull() ? Optional.empty() : Optional.of(val);
	}

	private Optional<BigDecimal> readOptionalBigDecimal(ResultSet rs, String column) throws SQLException {
		BigDecimal val = rs.getBigDecimal(column);
		return Optional.ofNullable(val);
	}

	private Optional<Divisa> readOptionalDivisa(ResultSet rs, String column) throws SQLException {
		String val = rs.getString(column);
		return val == null ? Optional.empty() : Optional.of(Divisa.getBySymbol(val));
	}

}

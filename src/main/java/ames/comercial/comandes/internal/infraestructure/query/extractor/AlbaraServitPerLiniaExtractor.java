package ames.comercial.comandes.internal.infraestructure.query.extractor;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.response.AlbaraServitLiniaComanda;
import ames.comercial.comandes.response.AlbaraServitLiniaComandaImpl;
import ames.comercial.server.Json;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.InformacioEnviament;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agrupa els albarans servits per línia de comanda. La clau del mapa és el
 * {@link KeyLiniaComanda} (comanda + numero), de manera que {@link ames.comercial.comandes.internal.application.query.ObtenirLiniesComanda}
 * pot adjuntar a cada {@link ames.comercial.comandes.response.ItemLiniaComanda} la seva llista d'albarans.
 */
public class AlbaraServitPerLiniaExtractor implements ResultSetExtractor<Map<KeyLiniaComanda, List<AlbaraServitLiniaComanda>>> {

	private final Json json;

	public AlbaraServitPerLiniaExtractor(Json json) {
		this.json = json;
	}

	@Override
	public Map<KeyLiniaComanda, List<AlbaraServitLiniaComanda>> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<KeyLiniaComanda, List<AlbaraServitLiniaComanda>> resultat = new HashMap<>();
		while (rs.next()) {
			var informacioEnviament = json.deserialize(rs.getString("informacio_enviament"), InformacioEnviament.class);

			var albara = AlbaraServitLiniaComandaImpl.builder()
					.albara(String.valueOf(rs.getLong("albara")))
					.albaraEspecial(MapperUtils.readOptionalString(rs, "albara_especial"))
					.dataAlbara(MapperUtils.readOptionalDate(rs, "data_albara"))
					// Format Advantage (FFIIIDDDD) igual que l'antic camp albenv
					.enviamentAlbara(informacioEnviament != null ? informacioEnviament.comenv() : "")
					.quantitat(rs.getLong("quantitat"))
					.factura(MapperUtils.readOptionalString(rs, "factures"))
					.entregat(isEntregat(rs.getString("informacio_magatzem")))
					.build();

			var clau = KeyLiniaComanda.of(rs.getLong("comanda"), rs.getLong("numero"));
			resultat.computeIfAbsent(clau, k -> new ArrayList<>()).add(albara);
		}
		return resultat;
	}

	/**
	 * Llegeix el camp {@code isEntregat} del JSON d'informació de magatzem sense dependre del
	 * tipus intern del mòdul albarans (evita violar els límits de mòdul de Spring Modulith).
	 */
	private boolean isEntregat(String informacioMagatzemJson) {
		if (informacioMagatzemJson == null || informacioMagatzemJson.isBlank())
			return false;
		JsonNode node = json.serialize(informacioMagatzemJson).get("isEntregat");
		return node != null && node.asBoolean();
	}

}

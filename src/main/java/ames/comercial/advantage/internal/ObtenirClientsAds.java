package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.advantage.internal.response.QueryClientResponse;
import ames.comercial.advantage.internal.response.QueryClientResponseImpl;
import ames.comercial.server.MapperUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ObtenirClientsAds {

	public List<QueryClientResponse> query(String filtre, String responsables, boolean incloureInactius, String colOrder, boolean asc) {
		PreparedStatementProvider prep = conn -> {

			String[] responsablesArray =
					(responsables == null || responsables.isBlank())
							? new String[0]
							: responsables.split(",");

			String inClause = String.join(",",
					Collections.nCopies(responsablesArray.length, "?")
			);

			String responsablesSql =
					responsablesArray.length == 0
							? ""
							: " OR c6.usuresp IN (" + inClause + ")";

			var statement = conn.prepareStatement(AdvantageDao.queryOrder("""
					SELECT c6.clicod, c6.clinom, cliali, clizon, clinif, cliflg, c6.empcod, e.descrip, c8.clipro, c6.bloimp, c6.usuresp, tpaisos.nom as nomPais
					FROM comundb.cli6 c6
					LEFT JOIN comundb.cli4 c4 ON c4.clicod4 = LEFT(c6.clicod, 4)
					LEFT JOIN comundb.empreses e ON c6.empcod = e.codi
					LEFT JOIN comundb.cli8 c8 ON c8.clicod = c6.clicod AND c6.empcod = c8.empcod
					INNER JOIN comundb.tpaisos ON c4.clipai = tpaisos.codpais
					WHERE (
							c6.clinom LIKE ?
						OR
							c6.cliali LIKE ?
						OR
							c6.clicod LIKE ?
						OR
							c4.clinif LIKE ?
						OR
							c8.clipro LIKE ?)
						AND (? OR cliflg = 'A')
						AND (
							? = ''
							""" + responsablesSql + """
						)
			""", colOrder, asc));
			int i = 1;

			statement.setString(i++, filtre.toUpperCase() + "%");
			statement.setString(i++, filtre.toUpperCase() + "%");
			statement.setString(i++, filtre);
			statement.setString(i++, filtre.toUpperCase() + "%");
			statement.setString(i++, "%" + filtre.toUpperCase() + "%");

			statement.setBoolean(i++, incloureInactius);

			statement.setString(i++, responsables == null ? "" : responsables);

			if (responsables != null && !responsables.isBlank()) {
				for (String r : responsables.split(",")) {
					statement.setString(i++, r);
				}
			}
	        return statement;
		};
		return new AdvantageDao().query(prep, mapeig());
	}

	private ResultSetAction<List<QueryClientResponse>> mapeig() {
		return rs -> {
			List<QueryClientResponse> resultat = new ArrayList<QueryClientResponse>();
			while (rs.next()) {
	        	resultat.add(QueryClientResponseImpl.builder()
	        		.codi(rs.getString("clicod"))
	        		.codiEmpresa(rs.getString("empcod"))
	        		.descEmpresa(rs.getString("descrip"))
	        		.nom(rs.getString("clinom"))
	        		.alias(rs.getString("cliali"))
	        		.zona(rs.getString("clizon"))
	        		.nif(Optional.ofNullable(rs.getString("clinif")).orElse(""))
					.codiProveidor(Optional.ofNullable(rs.getString("clipro")).orElse(""))
	        		.flag(rs.getString("cliflg"))
					.isImpagament(MapperUtils.readOptionalString(rs, "bloimp").map(bloimp -> bloimp.equals("S")).orElse(false))
	        		.usulogis(rs.getString("usuresp"))
					.nomPais(rs.getString("nomPais"))
	        		.build());
			}
			return resultat;
		};
	}
	
}

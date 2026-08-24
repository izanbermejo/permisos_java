package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.advantage.internal.response.QueryArticleNormalitzatResponse;
import ames.comercial.advantage.internal.response.QueryArticleNormalitzatResponseImpl;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.TipusArticleClient;

import java.util.ArrayList;
import java.util.List;

public class ObtenirArticlesNormalitzats {

	public List<QueryArticleNormalitzatResponse> query(String filtre, Empresa empresa, TipusArticleClient tipus) {
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement("""
					SELECT a.artint, a.clicod, a.aclfab, a.aclref, a.aclden, a.codfab, a.aclstk, a.aclqre,
					af.fitstkrac, af.comqres, a.aclucai, a.bosxcai, a.aclucap, a.tipus
					FROM comundb.artcli a
					LEFT JOIN artfit af on (a.artint=af.artint and a.clicod=af.clicod)
					WHERE (
						(a.clicod = '000000') -- Normalitzats
						AND tipus = ?	-- Tipus normalitzats
						AND	(? OR (CONTAINS(UPPER(a.aclref), ?)) OR (CONTAINS(UPPER(a.aclfab), ?))) -- Filtre de la referència i codi fàbrica
						AND (a.aclflg='A')	-- Només articles actius 
						AND (af.magcod= ?) AND (af.empcod=?) -- Empresa+Magatzem des d'on veure l'stock
						)
					ORDER BY  aclref;
			""");
			var empresaStock = Empresa.referenciaStock(empresa);
			var index = 1;
			statement.setString(index++, tipus.clauAdvantage());
			statement.setBoolean(index++, filtre.isBlank());
			statement.setString(index++, "*" + filtre.toUpperCase() + "*");
			statement.setString(index++, "*" + filtre.toUpperCase() + "*");
			statement.setString(index++, empresaStock.magatzem());
			statement.setString(index++, empresaStock.clau());
	        return statement;
		};
		return new AdvantageDao().query(prep, mapeig());
	}
	
	private ResultSetAction<List<QueryArticleNormalitzatResponse>> mapeig() {
		return rs -> {
			List<QueryArticleNormalitzatResponse> resultat = new ArrayList<QueryArticleNormalitzatResponse>();
			while (rs.next()) {
	        	resultat.add(QueryArticleNormalitzatResponseImpl.builder()
	        		.artint(rs.getString("artint"))
	        		.clicod(rs.getString("clicod"))
	        		.codi(rs.getString("aclfab"))
	        		.referencia(rs.getString("aclref"))
	        		.denominacio(rs.getString("aclden"))
	        		.fabrica(rs.getString("codfab"))
	        		.stock(rs.getLong("aclstk"))
	        		.reserva(rs.getLong("aclqre"))
	        		.stockLocal(rs.getLong("fitstkrac"))
	        		.reservaLocal(rs.getLong("comqres"))
	        		.unitatsEmbalatge(rs.getLong("aclucai"))
					.bossesCaixa(rs.getLong("bosxcai"))
					.caixesPalet(rs.getLong("aclucap"))
	        		.build());
			}
			return resultat;
		};
	}
	
}

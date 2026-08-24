package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.inventari.ext.FitxaMagatzemSatelitResponseImpl;
import ames.comercial.inventari.ext.IObtenirFitxesMagatzem.FitxaMagatzemSatelitResponse;
import ames.comercial.shared.KeyArticleClient;

import java.util.ArrayList;
import java.util.List;

public class ObtenirStockEstantsSatelitAds {
		
	public List<FitxaMagatzemSatelitResponse> query(KeyArticleClient articleClient) {
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement("""
					SELECT l.magcod, SUM(quant) as quant
					FROM localit l
					LEFT JOIN estante e ON l.magcod = e.magcod AND l.estante = e.estante
					WHERE artint = ? AND codcli = ? AND e.satelit = 'S'
					GROUP BY l.magcod;
					""");
			statement.setString(1, articleClient.artint());
			statement.setString(2, articleClient.clicod());
			return statement;
		};
		ResultSetAction<List<FitxaMagatzemSatelitResponse>> rsAction = rs -> {
			List<FitxaMagatzemSatelitResponse> resultat = new ArrayList<>();
			while (rs.next()) {
				resultat.add(FitxaMagatzemSatelitResponseImpl.builder()
								.magatzem(rs.getString("magcod"))
								.stock(rs.getLong("quant"))
						.build());
			}
			return resultat;
		};
		return new AdvantageDao().query(AdvantageDao.connectionMag, prep, rsAction);
	}
	
}

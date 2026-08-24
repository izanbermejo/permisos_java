package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class ObtenirPreusAds implements IObtenirPreusAds {

	@Override
	public Map<KeyArticleClient, Preu> query(Collection<KeyArticleClient> articlesClient) {
		if (articlesClient.isEmpty())
			return Map.of();
		var conjuntArticlesClient = Set.copyOf(articlesClient);
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement(generateSql(conjuntArticlesClient));
			int index = 1;
			for (var s : conjuntArticlesClient) {
				statement.setString(index++, s.artint());
				statement.setString(index++, s.clicod());
			}
			return statement;
		};
		ResultSetAction<Map<KeyArticleClient, Preu>> rsAction = rs -> {
			var resultat = new HashMap<KeyArticleClient, Preu>();
			while (rs.next()) {
				resultat.put(KeyArticleClient.of(rs.getString("artint"),rs.getString("clicod")),
						Preu.of(rs.getBigDecimal("aclpre"), rs.getString("acldiv")));
			}
			return resultat;
		};
		return new AdvantageDao().query(prep, rsAction);
	}

	private String generateSql(Set<KeyArticleClient> articlesClients) {
		var str = new StringBuilder("""
				SELECT artint, clicod, aclpre, acldiv FROM comundb.artcli a
				left join dummy d on a.artint = d.str1 
				WHERE 1=1 AND (
				""");
		// Condició d'articles clients
		for (var s : articlesClients) {
			str.append("(artint = ? AND clicod = ?) OR ");
		}
		var result = str.toString();
		return result.substring(0, result.length()-3) + ")";
	}
	
}

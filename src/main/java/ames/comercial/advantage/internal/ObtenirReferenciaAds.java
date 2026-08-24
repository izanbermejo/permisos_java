package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.shared.KeyArticleClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ObtenirReferenciaAds {

	public Map<KeyArticleClient, String> query(Set<KeyArticleClient> articles) { 
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement(
					String.format("""
							select artint, clicod, aclref
							from comundb.artcli a
							left join dummy d on a.artint = d.str1
							where artint IN(%s);
							""", articles.stream().map(v -> "?").collect(Collectors.joining(", "))));
			int index = 1;
			for (var s : articles) {
				statement.setString(index++, s.artint());
			}
	        return statement;
		};
		ResultSetAction<Map<KeyArticleClient, String>> rsAction = rs -> {
			Map<KeyArticleClient, String> resultat = new HashMap<KeyArticleClient, String>();
			while (rs.next()) {
	        	resultat.put(KeyArticleClient.of(rs.getString("artint"),rs.getString("clicod")), rs.getString("aclref"));
			}
			return resultat;
		};
		return new AdvantageDao().query(prep, rsAction);
	}
	
}

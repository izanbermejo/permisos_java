package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ObtenirPesPremsatAds {

	public BigDecimal query(String artint) {
		var valor = query(Set.of(artint)).get(artint);
		return valor != null ? valor : BigDecimal.ZERO;
	}

	public Map<String, BigDecimal> query(Set<String> articles) { 
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement(
					String.format("""
							select artint, artppre
							from comundb.art a
							left join dummy d on a.artint = d.str1
							where artint IN(%s);
							""", String.join(",", Collections.nCopies(articles.size(), "?"))));
			int index = 1;
			for (var s : articles) {
				statement.setString(index++, s);
			}
	        return statement;
		};
		ResultSetAction<Map<String, BigDecimal>> rsAction = rs -> {
			Map<String, BigDecimal> resultat = new HashMap<String, BigDecimal>();
			while (rs.next()) {
	        	resultat.put(rs.getString("artint"), rs.getBigDecimal("artppre"));
			}
			return resultat;
		};
		return new AdvantageDao().query(prep, rsAction);
	}
	
}

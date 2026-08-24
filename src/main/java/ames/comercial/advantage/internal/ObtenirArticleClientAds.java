package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.advantage.internal.response.QueryArticleClientResponse;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class ObtenirArticleClientAds {

	public Optional<QueryArticleClientResponse> query (String article, String client) {
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement("""
					SELECT
						a.aclref, a.aclfab, a.aclden, a.artint, a.aclflg, a.clicod, a.empcod, a.aclnvt, a.codfab,
						a.aclsec, f.descrip, a.aclpre, a.acldiv, a.bloqueostk, a.usubloq,
						a.aclucai, a.aclucap, a.bosxcai, a.tipus, a.acldel,
						COALESCE(a.artara, art.artara) AS "codiPartida",
						par.partida,
						par.nom as "descpartara"
					FROM comundb.artcli a
					LEFT JOIN fab f ON a.codfab = f.fabcod
					LEFT JOIN comundb.art art ON art.artint = a.artint
					LEFT JOIN comundb.partara par ON COALESCE(a.artara, art.artara) = par.codi
					WHERE aclfab = ? AND clicod = ?
					""");
			statement.setString(1, article);
			statement.setString(2, client);
			return statement;
		};
		ResultSetAction<Optional<QueryArticleClientResponse>> rsAction = rs -> {
			if (rs.next()) {
				return Optional.of(QueryArticleClientResponse.mapTo(rs));
			}
			return Optional.empty();
		};
		return new AdvantageDao().query(prep, rsAction);
	}

	public Optional<QueryArticleClientResponse> query(KeyArticleClient articleClient) {
		var listResultats = query(Set.of(articleClient));
		if (listResultats.isEmpty())
			return Optional.empty();
		if (listResultats.size() > 1)
			throw new AppException("ArticleClient amb mes d'un registre a l'artcli");
		return Optional.of(listResultats.get(0));
	}

	public List<QueryArticleClientResponse> query(Set<KeyArticleClient> articlesClient) {
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement(generateSql((articlesClient)));
			int index = 1;
			for (var s : articlesClient) {
				statement.setString(index++, s.artint());
				statement.setString(index++, s.clicod());
			}
			return statement;
		};
		ResultSetAction<List<QueryArticleClientResponse>> rsAction = rs -> {
			List<QueryArticleClientResponse> resultat = new ArrayList<QueryArticleClientResponse>();
			while (rs.next()) {
				resultat.add(QueryArticleClientResponse.mapTo(rs));
			}
			return resultat;
		};
		return new AdvantageDao().query(prep, rsAction);
	}

	private String generateSql(Set<KeyArticleClient> articlesClients) {
		var str = new StringBuilder("""
					SELECT 
					a.aclref, a.aclfab, a.aclden, a.artint, a.aclflg, a.clicod, a.empcod, a.aclnvt, a.codfab,
					a.aclsec, f.descrip, a.aclpre, a.acldiv, a.bloqueostk, a.usubloq,
					a.aclucai, a.aclucap, a.bosxcai, a.tipus, a.acldel,
					COALESCE(a.artara, art.artara) AS "codiPartida",
					par.partida,
					par.nom as "descpartara"
					FROM comundb.artcli a
					LEFT JOIN fab f ON a.codfab = f.fabcod
					LEFT JOIN comundb.art art ON art.artint = a.artint
					LEFT JOIN comundb.partara par ON COALESCE(a.artara, art.artara) = par.codi
					WHERE 1=1 AND (
				""");
		// Condició d'articles clients
		for (var s : articlesClients) {
			str.append("(a.artint = ? AND clicod = ?) OR ");
		}
		var result = str.toString();
		return result.substring(0, result.length()-3) + ")";
	}
			
}

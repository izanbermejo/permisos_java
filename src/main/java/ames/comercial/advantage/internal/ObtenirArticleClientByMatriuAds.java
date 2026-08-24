package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.advantage.internal.response.QueryArticleClientResponse;

import java.util.Optional;

public class ObtenirArticleClientByMatriuAds {

	public Optional<QueryArticleClientResponse> query(String client, String matriu) {
		// Es busca pels 6 primers caràcters de la matriu ja que sinó pot ser que un client tingui la mateixa matriu
		// per a diferents Engineering Codes (123400A i 123400B per exemple) i això faria que no es trobés l'article client
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
					WHERE clicod = ?
						AND SUBSTRING(aclfab, 1, 6) = ?	-- Només els 6 primers caràcters de la matriu (no es té en compte l'Enginering Code)
						AND aclflg = 'A'
					""");
			int index = 1;
			statement.setString(index++, client);
			statement.setString(index++, matriu.substring(0, 6));
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

}

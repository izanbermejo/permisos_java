package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.response.QueryArticleClientEDIResponse;
import ames.comercial.advantage.internal.response.QueryArticleClientEDIResponseImpl;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ObtenirArticleClientEDIAds {

	public List<QueryArticleClientEDIResponse> findByNADAndArtRef(String nad, String artRef) {
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement("""
										SELECT 
											a.aclref, a.artint, a.aclfab, a.clicod,c.nad02 , c.usuresp, c.clinom, a.aclmags, a.aclmage,
											a.aclstk, a.aclucai,a.bosxcai, a.aclucap 
										FROM comundb.artcli a 
										LEFT JOIN comundb.cli6 c ON a.clicod = c.clicod
										WHERE a.aclref=? 
										AND c.nad02=? 
										AND a.aclflg = 'A'
			""");
			statement.setString(1, artRef);
			statement.setString(2, nad);
	        return statement;
		};
		return new AdvantageDao().query(prep, mapeig());
	}

	public List<QueryArticleClientEDIResponse> findByArtRef(String artRef) {
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement("""
										SELECT 
											a.aclref, a.artint, a.aclfab, a.clicod,c.nad02 , c.usuresp, c.clinom, a.aclmags, a.aclmage,
											a.aclstk, a.aclucai,a.bosxcai, a.aclucap
										FROM comundb.artcli a 
										LEFT JOIN comundb.cli6 c ON a.clicod = c.clicod 
										WHERE a.aclref=? 
										AND a.aclflg = 'A'
			""");
			statement.setString(1, artRef);
			return statement;
		};
		return new AdvantageDao().query(prep, mapeig());
	}

	public List<QueryArticleClientEDIResponse> findByCliCodAclRef(String cliCod, String aclRef) {
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement("""
										SELECT 
											a.aclref, a.artint, a.aclfab, a.clicod,c.nad02 , c.usuresp, c.clinom, a.aclmags, a.aclmage,
											a.aclstk, a.aclucai,a.bosxcai, a.aclucap
										FROM comundb.artcli a 
										LEFT JOIN comundb.cli6 c ON a.clicod = c.clicod 
										WHERE a.aclref=? 
										AND a.clicod=?
										AND a.aclflg = 'A'
			""");
			statement.setString(1, aclRef);
			statement.setString(2, cliCod);
			return statement;
		};
		return new AdvantageDao().query(prep, mapeig());
	}

	public List<QueryArticleClientEDIResponse> findByCliCodsAclRef(List<String> cliCods, String aclRef) {
		if (cliCods == null || cliCods.isEmpty()) {
			return Collections.emptyList();
		}

		String placeholders = cliCods.stream()
				.map(c -> "?")
				.collect(Collectors.joining(","));

		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement("""
                SELECT
                    a.aclref, a.artint, a.aclfab, a.clicod, c.nad02, c.usuresp, c.clinom, a.aclmags, a.aclmage,
                    a.aclstk, a.aclucai, a.bosxcai, a.aclucap
                FROM comundb.artcli a
                LEFT JOIN comundb.cli6 c ON a.clicod = c.clicod
                WHERE a.aclref = ?
                  AND a.clicod IN (""" + placeholders + """
                )
                  AND a.aclflg = 'A'
        """);
			statement.setString(1, aclRef);

			for (int i = 0; i < cliCods.size(); i++) {
				statement.setString(i + 2, cliCods.get(i));
			}

			return statement;
		};

		return new AdvantageDao().query(prep, mapeig());
	}

//	public List<QueryArticleClientEDIResponse> findByArtInt(String artInt) {
//		PreparedStatementProvider prep = conn -> {
//			var statement = conn.prepareStatement("""
//										SELECT
//											a.aclref, a.artint, a.aclfab, a.clicod,c.nad02 , c.usuresp, c.clinom, a.aclmags, a.aclmage
//										FROM comundb.artcli a
//										LEFT JOIN comundb.cli6 c ON a.clicod = c.clicod
//										WHERE a.aclint=?
//										AND a.aclflg = 'A'
//			""");
//			statement.setString(1, artInt);
//			return statement;
//		};
//		return new AdvantageDao().query(prep, mapeig());
//	}
//
//
//	public List<QueryArticleClientEDIResponse> findByNADAndEdiBoxAndArtRef(String nad,String edibox, String artRef) {
//		PreparedStatementProvider prep = conn -> {
//			var statement = conn.prepareStatement("""
//					SELECT
//						a.aclref, a.artint, a.aclfab, a.clicod,c.nad02 , c.usulogis, c.clinom, a.aclmags, a.aclmage
//					FROM comundb.artcli a
//					LEFT JOIN comundb.cli6 c ON a.clicod = c.clicod
//					WHERE 	a.aclref=?
//					AND c.nad02=?
//					AND c.edibox=?
//					AND a.aclflg = 'A'
//			""");
//			statement.setString(1, artRef);
//			statement.setString(2, nad);
//			statement.setString(3, edibox);
//
//			return statement;
//		};
//		return new AdvantageDao().query(prep, mapeig());
//	}

	private AdvantageDao.ResultSetAction<List<QueryArticleClientEDIResponse>> mapeig() {
		return rs -> {
			List<QueryArticleClientEDIResponse> resultat = new ArrayList<QueryArticleClientEDIResponse>();
			while (rs.next()) {
				resultat.add(QueryArticleClientEDIResponseImpl.builder()
						.artInt(rs.getString("artint"))
						.referencia(rs.getString("aclref"))
						.aclFab(rs.getString("aclfab"))
						.clicod(rs.getString("clicod"))
						.nomClient(rs.getString("clinom"))
						.usuariLogistica(rs.getString("usuresp"))
						.magatzemSortida(rs.getString("aclmags"))
						.magatzemEntrada(rs.getString("aclmage"))
						.unitatsEmbalatge(rs.getLong("aclucai"))
						.bossesCaixa(rs.getInt("bosxcai"))
						.caixesPalet(rs.getInt("aclucap"))
						.stock(rs.getLong("aclstk"))
						.build());
			}
			return resultat;
		};
	}
}

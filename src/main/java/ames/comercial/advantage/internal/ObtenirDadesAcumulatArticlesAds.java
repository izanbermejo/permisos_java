package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Component
public class ObtenirDadesAcumulatArticlesAds {
		
	public Optional<DadesAcumulatArticle> query(KeyArticleClient articleClient) {
//		System.out.println("artint: " + articleClient.artint() + " / cliclod: " + articleClient.clicod());
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement("""
					SELECT
							stksal, albacum, datacum
							FROM comundb.artcli a
							left join dummy d on a.artint = d.str1
							WHERE
								a.artint = ?
								AND a.clicod = ?
					""");
			statement.setString(1, articleClient.artint());
			statement.setString(2, articleClient.clicod());
			return statement;
		};
		AdvantageDao.ResultSetAction<Optional<ObtenirDadesAcumulatArticlesAds.DadesAcumulatArticle>> rsAction = rs -> rs.next() ? Optional.of(mapper(rs)) : Optional.empty();
		return new AdvantageDao().query(prep, rsAction);
	}

	private ObtenirDadesAcumulatArticlesAds.DadesAcumulatArticle mapper(ResultSet rs) throws SQLException {
		return new ObtenirDadesAcumulatArticlesAds.DadesAcumulatArticle(rs.getString("datacum"),
				rs.getString("stksal"),rs.getString("albacum"));
	}

	public record DadesAcumulatArticle(String dataAcumulat, String stockAcumulat, String albaraAcumulat) {}
}

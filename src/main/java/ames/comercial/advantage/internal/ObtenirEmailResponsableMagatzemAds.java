package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirEmailResponsableMagatzemAds;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirEmailResponsableMagatzemAds implements IObtenirEmailResponsableMagatzemAds {

	@Override
	public Optional<String> query (String magatzem) {
		PreparedStatementProvider prep = conn -> {
			var statement = conn.prepareStatement("""
					SELECT emailmag
					FROM magatzems m
					LEFT JOIN com.dummy d ON m.emailmag = d.str1
					WHERE magcod = ?
					""");
			statement.setString(1, magatzem);
			return statement;
		};
		ResultSetAction<Optional<String>> rsAction = rs -> {
			if (rs.next()) {
				return Optional.of(rs.getString("emailmag"));
			}
			return Optional.empty();
		};
		return new AdvantageDao().query(AdvantageDao.connectionMag, prep, rsAction);
	}
			
}

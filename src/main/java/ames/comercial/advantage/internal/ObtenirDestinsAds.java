package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirDestinsTransport;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirDestinsAds implements IObtenirDestinsTransport {

    @Override
    public Optional<DestiAds> get(String codi) {
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select *
						from comundb.destitra t
						LEFT JOIN dummy d ON t.coddesti = d.str1
						where coddesti = ?
					""");
            statement.setString(1, codi);
            return statement;
        };
        AdvantageDao.ResultSetAction<Optional<DestiAds>> rsAction = rs -> rs.next() ? Optional.of(mapper(rs)) : Optional.empty();
        return new AdvantageDao().query(prep, rsAction);
    }

    @Override
    public List<DestiAds> all() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
						select *
						from comundb.destitra t
						LEFT JOIN dummy d ON t.coddesti = d.str1
      """);
        AdvantageDao.ResultSetAction<List<DestiAds>> rsAction = rs -> {
            var result = new ArrayList<DestiAds>();
            while (rs.next()) {
                result.add(mapper(rs));
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private DestiAds mapper(ResultSet rs) throws SQLException {
        return new DestiAds(rs.getString("coddesti"),
                rs.getString("nom"));
    }

    public record DestiAds(String codi, String nom) {}
}

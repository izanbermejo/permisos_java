package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirTransportistesAds;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirTransportistesAds implements IObtenirTransportistesAds {

    @Override
    public Optional<TransportistaAds> get(String codi) {
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select *
						from comundb.tra t
						LEFT JOIN dummy d ON t.tracod = d.str1
						where tracod = ?
					""");
            statement.setString(1, codi);
            return statement;
        };
        AdvantageDao.ResultSetAction<Optional<TransportistaAds>> rsAction = rs -> rs.next() ? Optional.of(mapper(rs)) : Optional.empty();
        return new AdvantageDao().query(prep, rsAction);
    }

    @Override
    public List<TransportistaAds> all() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
						select *
						from comundb.tra t
						LEFT JOIN dummy d ON t.tracod = d.str1
      """);
        AdvantageDao.ResultSetAction<List<TransportistaAds>> rsAction = rs -> {
            var result = new ArrayList<TransportistaAds>();
            while (rs.next()) {
                result.add(mapper(rs));
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private TransportistaAds mapper(ResultSet rs) throws SQLException {
        return new TransportistaAds(rs.getString("tracod"),
                rs.getString("descrip"),
                rs.getString("nif"),
                rs.getString("adreca"),
                rs.getString("poblacio"),
                rs.getString("codpos"),
                rs.getString("pais"));
    }

    public record TransportistaAds(String codi, String descripcio, String nif, String adreca, String poblacio,
                                   String codiPostal, String pais) {}
}

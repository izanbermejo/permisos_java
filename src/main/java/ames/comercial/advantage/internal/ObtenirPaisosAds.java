package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirPaisosAds;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirPaisosAds implements IObtenirPaisosAds {

    @Override
    public Optional<PaisAds> get(String codi) {
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select *
						from comundb.tpaisos p
						LEFT JOIN dummy d ON p.codpais = d.str1
						where codPais = ?
					""");
            statement.setString(1, codi);
            return statement;
        };
        AdvantageDao.ResultSetAction<Optional<PaisAds>> rsAction = rs -> rs.next() ? Optional.of(mapper(rs)) : Optional.empty();
        return new AdvantageDao().query(prep, rsAction);
    }

    @Override
    public List<PaisAds> all() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
						select *
						from comundb.tpaisos p
						LEFT JOIN dummy d ON p.codpais = d.str1
        """);
        AdvantageDao.ResultSetAction<List<PaisAds>> rsAction = rs -> {
            var result = new ArrayList<PaisAds>();
            while (rs.next()) {
                result.add(mapper(rs));
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private PaisAds mapper(ResultSet rs) throws SQLException {
        return new PaisAds(rs.getString("codpais"),
                rs.getString("codiso"),
                rs.getString("nom"),
                rs.getString("tarifa"),
                rs.getString("cmat"));
    }

    public record PaisAds(String codi, String iso, String nom, String tarifa, String tarifaMedical) {}

}

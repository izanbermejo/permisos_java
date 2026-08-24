package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirZonesTransportAds;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirZonesTransportAds implements IObtenirZonesTransportAds {

    @Override
    public Optional<ZonaTransportAds> get(String codiZona, String codiTransportista, String codiPais) {
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select *
						from zontra z
						LEFT JOIN dummy d ON z.codzon = d.str1
						where codzon = ? AND codtra = ? AND codPai = ?
					""");
            statement.setString(1, codiZona);
            statement.setString(2, codiTransportista);
            statement.setString(3, codiPais);
            return statement;
        };
        AdvantageDao.ResultSetAction<Optional<ZonaTransportAds>> rsAction = rs -> rs.next() ? Optional.of(mapper(rs)) : Optional.empty();
        return new AdvantageDao().query(prep, rsAction);
    }

    @Override
    public List<ZonaTransportAds> get(String codiTransportista, String codiPais) {
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select *
						from zontra z
						LEFT JOIN dummy d ON z.codzon = d.str1
						where codtra = ? AND (codPai = ? OR codPai IS NULL OR codPai = '')
					""");
            statement.setString(1, codiTransportista);
            statement.setString(2, codiPais);
            return statement;
        };
        AdvantageDao.ResultSetAction<List<ZonaTransportAds>> rsAction = rs -> {
            var result = new ArrayList<ZonaTransportAds>();
            while (rs.next()) {
                result.add(mapper(rs));
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    @Override
    public List<ZonaTransportAds> all() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
						select *
						from zontra z
						LEFT JOIN dummy d ON z.codzon = d.str1
        """);
        AdvantageDao.ResultSetAction<List<ZonaTransportAds>> rsAction = rs -> {
            var result = new ArrayList<ZonaTransportAds>();
            while (rs.next()) {
                result.add(mapper(rs));
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private ZonaTransportAds mapper(ResultSet rs) throws SQLException {
        return new ZonaTransportAds(rs.getString("codzon"),
                rs.getString("codtra"),
                rs.getString("codpai"),
                rs.getString("deszon"));
    }

    public record ZonaTransportAds(String codi, String codiTransportista, String codiPais, String descripcio) {}

}

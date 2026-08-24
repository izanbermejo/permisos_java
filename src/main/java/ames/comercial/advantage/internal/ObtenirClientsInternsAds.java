package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirClientsInternsAds;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class ObtenirClientsInternsAds implements IObtenirClientsInternsAds {

    @Override
    public List<ClientIntern> get() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
						select clicod, descripcio
						from comundb.xfact x
						LEFT JOIN dummy d ON x.clicod = d.str1
						""");
        AdvantageDao.ResultSetAction<List<ClientIntern>> rsAction = rs -> {
            var result = new ArrayList<ClientIntern>();
            while (rs.next()) {
                result.add(mapper(rs));
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private ClientIntern mapper(ResultSet rs) throws SQLException {
        return new ClientIntern(rs.getString("clicod"),
                rs.getString("descripcio"));
    }

    public record ClientIntern(String codi, String descripcio) {}

}

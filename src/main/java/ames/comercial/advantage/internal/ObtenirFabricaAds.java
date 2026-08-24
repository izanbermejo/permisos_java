package ames.comercial.advantage.internal;

import ames.comercial.advantage.FabricaAdsImpl;
import ames.comercial.advantage.IObtenirFabricaAds;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirFabricaAds implements IObtenirFabricaAds {

    @Override
    public Optional<FabricaAds> get(String codi) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement("SELECT * FROM fab WHERE fabcod = ?");
            statement.setString(1, codi);
            return statement;
        };
        ResultSetAction<Optional<FabricaAds>> rsAction = rs -> rs.next() ? Optional.of(mapper(rs)) : Optional.empty();
        return new AdvantageDao().query(prep, rsAction);
    }

    @Override
    public List<FabricaAds> all() {
        PreparedStatementProvider prep = conn -> conn.prepareStatement("SELECT * FROM fab");
        ResultSetAction<List<FabricaAds>> rsAction = rs -> {
            var result = new ArrayList<FabricaAds>();
            while (rs.next()) {
                result.add(mapper(rs));
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private FabricaAds mapper(ResultSet rs) throws SQLException {
        return FabricaAdsImpl.builder()
                .codi(rs.getString("fabcod"))
                .descripcio(rs.getString("descrip"))
                .magatzemEntrada(rs.getString("magent"))
                .build();
    }

}

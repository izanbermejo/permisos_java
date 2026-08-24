package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirTarifesDefinidesAds;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import com.google.common.base.Strings;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Component
public class ObtenirTarifesDefinidesAds implements IObtenirTarifesDefinidesAds {

    @Override
    public Optional<TarifesDefinidesAdsResponse> get(String clicod) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select cli6.tarifa, cli6.tarifab, tpaisos.tarifa as tarifaPais, tpaisos.tarifab as tarifaBPais,
						    tpaisos.cmat as tarifaCmatPais
						from comundb.cli6
						INNER JOIN comundb.cli4 ON SUBSTRING(cli6.clicod,1,4) = cli4.clicod4
						INNER JOIN comundb.tpaisos ON clipai = codpais
						WHERE cli6.clicod = ?
					""");
            statement.setString(1, clicod);
            return statement;
        };
        ResultSetAction<Optional<TarifesDefinidesAdsResponse>> rsAction = rs -> {
            if (rs.next()) {
                return Optional.of(
                        new TarifesDefinidesAdsResponse(readOptionalString(rs, "tarifa"),
                                readOptionalString(rs, "tarifab"),
                                readOptionalString(rs, "tarifaPais"),
                                readOptionalString(rs, "tarifaBPais"),
                                readOptionalString(rs, "tarifaCmatPais")));
            }
            return Optional.empty();
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private Optional<String> readOptionalString(ResultSet rs, String column) throws SQLException {
        var s = rs.getString(column);
        return Strings.isNullOrEmpty(s) ? Optional.empty() : Optional.of(s);
    }

    public record TarifesDefinidesAdsResponse(Optional<String> tarifaCli6, Optional<String> tarifaBCli6,
                                              Optional<String> tarifaPais, Optional<String> tarifaBPais,
                                              Optional<String> tarifaCmatPais) {
    }

}

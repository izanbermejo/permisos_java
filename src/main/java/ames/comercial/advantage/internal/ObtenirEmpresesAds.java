package ames.comercial.advantage.internal;

import ames.comercial.advantage.EmpresaAdsImpl;
import ames.comercial.advantage.IObtenirEmpresesAds;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirEmpresesAds implements IObtenirEmpresesAds {

    @Override
    public Optional<EmpresaAds> get(String codi) {
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select *
						from comundb.empreses e
						LEFT JOIN dummy d ON e.codi = d.str1
						where codi = ?
					""");
            statement.setString(1, codi);
            return statement;
        };
        AdvantageDao.ResultSetAction<Optional<EmpresaAds>> rsAction = rs -> rs.next() ? Optional.of(mapper(rs)) : Optional.empty();
        return new AdvantageDao().query(prep, rsAction);
    }

    @Override
    public List<EmpresaAds> all() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
						select *
						from comundb.empreses e
						LEFT JOIN dummy d ON e.codi = d.str1
      """);
        AdvantageDao.ResultSetAction<List<EmpresaAds>> rsAction = rs -> {
            var result = new ArrayList<EmpresaAds>();
            while (rs.next()) {
                result.add(mapper(rs));
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private EmpresaAds mapper(ResultSet rs) throws SQLException {
        return EmpresaAdsImpl.builder()
                .codi(rs.getString("codi"))
                .descripcio(rs.getString("descrip"))
                .adresaAlbara(rs.getString("diralb"))
                .poblacioAlbara(rs.getString("pobalb"))
                .paisAlbara(rs.getString("paialb"))
                .gateComp(rs.getString("gatecomp"))
                .dunsEnviament(rs.getString("dunsship"))
                .telalb(nullToEmpty(rs.getString("telalb")))
                .nif(nullToEmpty(rs.getString("nif")))
                .build();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

}

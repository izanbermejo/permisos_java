package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirNumeradorAlbara;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import org.springframework.stereotype.Component;

@Component
public class ObtenirNumeradorAlbara implements IObtenirNumeradorAlbara {

    @Override
    public long obtenir(String empresa) {
        AdvantageDao adsDao = new AdvantageDao();
        // S'incrementa en un 1 el comptador
        PreparedStatementProvider incrementNumerador = conn -> {
            var statement = conn.prepareStatement("UPDATE comundb.empreses SET ultalb = ultalb+1 WHERE codi = ?");
            statement.setString(1, empresa);
            return statement;
        };
        adsDao.executeUpdate(incrementNumerador);
        // S'obté el numerador
        PreparedStatementProvider obtenirNumerador = conn -> {
            var statement = conn.prepareStatement("SELECT ultalb FROM comundb.empreses WHERE codi = ?");
            statement.setString(1, empresa);
            return statement;
        };
        ResultSetAction<Long> obtenirNumeradorAction = rs -> {
            if (rs.next())
                return rs.getLong("ultalb");
            return 1L;
        };
        return adsDao.query(obtenirNumerador, obtenirNumeradorAction);
    }

}

package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirNumeradorComanda;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import org.springframework.stereotype.Component;

@Component
public class ObtenirNumeradorComanda implements IObtenirNumeradorComanda {

    @Override
    public long obtenir() {
        AdvantageDao adsDao = new AdvantageDao();
        // S'incrementa en un 1 el comptador
        PreparedStatementProvider incrementNumerador = conn -> conn.prepareStatement("UPDATE par SET ultcom = ultcom+1 WHERE 1=1");
        adsDao.executeUpdate(incrementNumerador);
        // S'obté el numerador
        PreparedStatementProvider obtenirNumerador = conn -> conn.prepareStatement("SELECT TOP 1 ultcom FROM par;");
        ResultSetAction<Long> obtenirNumeradorAction = rs -> {
            if (rs.next())
                return rs.getLong("ultcom");
            return 1L;
        };
        return adsDao.query(obtenirNumerador, obtenirNumeradorAction);
    }

}

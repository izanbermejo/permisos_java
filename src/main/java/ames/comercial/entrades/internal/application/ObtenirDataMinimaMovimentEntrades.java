package ames.comercial.entrades.internal.application;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ObtenirDataMinimaMovimentEntrades {

    public LocalDate executar() {
        PreparedStatementProvider statement = connection ->
                connection.prepareStatement("""
                    SELECT datmov
                    FROM setup
                """);
        ResultSetAction<LocalDate> rsAction = rs -> {
            if (rs.next()) {
                return rs.getDate("datmov").toLocalDate();
            }
            return LocalDate.now();
        };
        return new AdvantageDao().query(statement, rsAction);
    }

}

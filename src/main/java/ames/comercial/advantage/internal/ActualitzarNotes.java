package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import org.springframework.stereotype.Component;

@Component
public class ActualitzarNotes {

    public void executarClient(String clicod, String text) {
        executar("UPDATE comundb.cli6 SET notes = ? WHERE clicod = ?", clicod, text);
    }

    public void executarLogistica(String clicod, String text) {
        executar("UPDATE comundb.cli6 SET notalb = ? WHERE clicod = ?", clicod, text);
    }

    public void executarMorositat(String clicod, String text) {
        executar("UPDATE comundb.cli6 SET obsmor = ? WHERE clicod = ?", clicod, text);
    }

    private void executar(String query, String clicod, String text) {
        AdvantageDao adsDao = new AdvantageDao();
        PreparedStatementProvider statement = connection -> {
            var prep = connection.prepareStatement(query);
            prep.setString(1, text);
            prep.setString(2,clicod);
            return prep;
        };
        adsDao.executeUpdate(statement);
    }

}

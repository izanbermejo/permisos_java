package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import org.springframework.stereotype.Component;

@Component
public class ActualitzarNotesEmbalatge {

    public void executar(String articleClient, String text) {
        AdvantageDao adsDao = new AdvantageDao();
        var projecte = articleClient.substring(0, 7);
        var client = articleClient.substring(7, 13);
        PreparedStatementProvider statement = connection -> {
            var prep = connection.prepareStatement("UPDATE comundb.artcli SET comlcom = ? WHERE aclfab = ? AND clicod = ?");
            prep.setString(1, text);
            prep.setString(2, projecte);
            prep.setString(3, client);
            return prep;
        };
        adsDao.executeUpdate(statement);
    }

}

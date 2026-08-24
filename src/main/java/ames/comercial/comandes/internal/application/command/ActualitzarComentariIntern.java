package ames.comercial.comandes.internal.application.command;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.stereotype.Component;

@Component
public class ActualitzarComentariIntern {

    public void executarClient(String cliCod, String text) {
        executar("UPDATE comundb.cli6 SET ALERTCOM = ? WHERE clicod = ?", text, cliCod);
    }

    public void executarArticle(KeyArticleClient key, String text) {
        executar("UPDATE comundb.artcli SET ALERTCOM = ? WHERE artint = ? AND clicod = ?",
                text,
                key.artint(),
                key.clicod()
        );
    }

    private void executar(String query, String... params) {
        AdvantageDao adsDao = new AdvantageDao();

        AdvantageDao.PreparedStatementProvider stmt = connection -> {
            var prep = connection.prepareStatement(query);

            for (int i = 0; i < params.length; i++) {
                prep.setString(i + 1, params[i]);
            }

            return prep;
        };

        adsDao.executeUpdate(stmt);
    }
}


package ames.comercial.advantage.internal.auxiliar.generators;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.shared.KeyArticleClient;

public class ArtCliGenerator {

    public static PreparedStatementProviderBatch updateAcumulatEntrades(KeyArticleClient articleClient, long quantitat) {
        return connection -> {
            var query = "UPDATE comundb.artcli SET aclacue = aclacue + ? WHERE artint = ? AND clicod = ?";
            var prep = connection.prepareStatement(query);
                prep.setLong(1, quantitat);
                prep.setString(2, articleClient.artint());
                prep.setString(3, articleClient.clicod());
                prep.addBatch();
            return prep;
        };
    }

}

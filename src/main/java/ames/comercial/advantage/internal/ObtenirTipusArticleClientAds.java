package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirTipusArticleClientAds;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.TipusArticleClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class ObtenirTipusArticleClientAds implements IObtenirTipusArticleClientAds {
    @Override
    public Optional<TipusArticleClient> get(KeyArticleClient articleClient) {
        var mapResp = get(Set.of(articleClient));
        return Optional.ofNullable(mapResp.get(articleClient));
    }

    @Override
    public Map<KeyArticleClient, TipusArticleClient> get(Set<KeyArticleClient> articlesClient) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(generateSql(articlesClient));
            int index = 1;
            for (var s : articlesClient) {
                statement.setString(index++, s.artint());
                statement.setString(index++, s.clicod());
            }
            return statement;
        };
        ResultSetAction<Map<KeyArticleClient, TipusArticleClient>> rsAction = rs -> {
            var resultat = new HashMap<KeyArticleClient, TipusArticleClient>();
            while (rs.next()) {
                resultat.put(KeyArticleClient.of(rs.getString("artint"),rs.getString("clicod")),
                        TipusArticleClient.getByTipus(rs.getString("tipus")));
            }
            return resultat;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private String generateSql(Set<KeyArticleClient> articlesClients) {
        var str = new StringBuilder("""
            SELECT artint, clicod, tipus 
            FROM comundb.artcli a
            LEFT JOIN dummy d ON a.artint = d.str1
            WHERE 1=1 AND (
            """);
        // Condició d'articles clients
        for (var s : articlesClients) {
            str.append("(artint = ? AND clicod = ?) OR ");
        }
        var result = str.toString();
        return result.substring(0, result.length()-3) + ")";
    }

}

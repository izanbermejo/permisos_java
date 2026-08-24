package ames.comercial.cache.stocksatelit;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.cache.CacheConfig;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StockSatelitService {

    public record RegStockSatelit (KeyArticleClient articleClient, String magatzem, int stock) {};

    @Cacheable(value = CacheConfig.STOCK_SATELIT, sync = true)
    public List<RegStockSatelit> obtenirRegistresStockSatelit() {
        PreparedStatementProvider prep = conn -> conn.prepareStatement("""
                SELECT artint, codcli, l.magcod, SUM(quant) as quant
                FROM localit l
                JOIN estante e ON l.magcod = e.magcod AND l.estante = e.estante
                WHERE e.satelit = 'S'
                GROUP BY artint, codcli, l.magcod;
                """);
        ResultSetAction<List<RegStockSatelit>> rsAction = rs -> {
            List<RegStockSatelit> resultat = new ArrayList<>();
            while (rs.next()) {
                resultat.add(new RegStockSatelit(KeyArticleClient.of(rs.getString("artint"), rs.getString("codcli")),
                        rs.getString("magcod"),
                        rs.getInt("quant")));
            }
            return resultat;
        };
        return new AdvantageDao().query(AdvantageDao.connectionMag, prep, rsAction);
    }

}

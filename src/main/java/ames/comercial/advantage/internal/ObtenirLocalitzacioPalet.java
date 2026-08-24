package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirLocalitzacioPalet;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class ObtenirLocalitzacioPalet implements IObtenirLocalitzacioPalet {

    @Override
    public List<PaletInfo> get(KeyArticleClient articleClient) {

        PreparedStatementProvider prep = conn -> {
            var st = conn.prepareStatement(
                    """
                                SELECT
                                    l.ETIPALE,
                                    COUNT(*) AS numCaixes,
                                    SUM(l.QUANT) AS numPeces,
                                    l.ESTANTE, l.DATAENT, l.MAGCOD, l.FABRICA,
                                    CONCAT(COALESCE(el.CODIELEM, ''),
                                    CONCAT(' ', COALESCE(el.DESCRIPCIO, ''))) as EMBALATGE
                                FROM localit l
                                LEFT JOIN emb.etiemba et ON l.ETICAJA = et.ETIQUETA
                                LEFT JOIN emb.elemba el ON el.CODIELEM = et.CODIELEM
                                WHERE l.ARTINT=? AND l.CODCLI=?
                                GROUP BY l.ETIPALE, l.ESTANTE, l.DATAENT, l.MAGCOD, el.CODIELEM, el.DESCRIPCIO, l.FABRICA
                                ORDER BY l.ETIPALE
                            """
            );
            st.setString(1, articleClient.artint());
            st.setString(2, articleClient.clicod());
            return st;
        };

        ResultSetAction<List<PaletInfo>> rsAction = rs -> {
            List<PaletInfo> result = new ArrayList<>();
            while (rs.next()) {
                result.add(
                        PaletInfoImpl.builder()
                                .peces(rs.getLong("numPeces"))
                                .estante(rs.getString("ESTANTE"))
                                .dataEntrada(rs.getDate("DATAENT"))
                                .palet(rs.getString("ETIPALE"))
                                .caixes(rs.getString("numCaixes"))
                                .codiMagatzem(rs.getString("MAGCOD"))
                                .embalatge(rs.getString("EMBALATGE"))
                                .fabrica(rs.getString("FABRICA"))
                                .build()
                );
            }
            return result;
        };

        return new AdvantageDao().query(AdvantageDao.connectionMag, prep, rsAction);
    }

    @JsonDeserialize(builder = PaletInfoImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface PaletInfo {
        long peces();
        String estante();
        Date dataEntrada();
        String palet();
        String caixes();
        String codiMagatzem();
        String embalatge();
        String fabrica();
    }
}

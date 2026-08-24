package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirFifoPeses;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ObtenirFifoPeses implements IObtenirFifoPeses {

    @Override
    public List<FifoPeses> get(KeyArticleClient articleClient) {

        PreparedStatementProvider prep = conn -> {
            var st = conn.prepareStatement(
                    """
                        SELECT l.DATA, l.LOT, l.QUANT, l.ESTANTE, l.DATAENT, l.ETICAJA, l.ETIPALE, l.FABRICA, l.MAGCOD,
                        CONCAT(COALESCE(el.CODIELEM, ''), CONCAT(' ', COALESCE(el.DESCRIPCIO, ''))) as EMBALATGE
                        FROM localit l
                        LEFT JOIN emb.etiemba et ON l.ETICAJA = et.ETIQUETA
                        LEFT JOIN emb.elemba el ON el.CODIELEM = et.CODIELEM
                        WHERE l.ARTINT=? AND l.CODCLI=?
                        ORDER BY l.ETICAJA, l.DATA
                    """
            );
            st.setString(1, articleClient.artint());
            st.setString(2, articleClient.clicod());
            return st;
        };

        ResultSetAction<List<FifoPeses>> rsAction = rs -> {
            List<FifoPeses> result = new ArrayList<>();
            while (rs.next()) {
                result.add(
                        FifoPesesImpl.builder()
                                .data(rs.getDate("DATA"))
                                .lot(rs.getString("LOT"))
                                .quantitat(rs.getLong("QUANT"))
                                .estante(rs.getString("ESTANTE"))
                                .dataEntrada(rs.getDate("DATAENT"))
                                .etiquetaCaixa(rs.getString("ETICAJA"))
                                .etiquetaPaler(rs.getString("ETIPALE"))
                                .fabrica(rs.getString("FABRICA"))
                                .codiMagatzem(rs.getString("MAGCOD"))
                                .embalatge(rs.getString("EMBALATGE"))
                                .build()
                );
            }
            return result;
        };

        return new AdvantageDao().query(AdvantageDao.connectionMag, prep, rsAction);
    }

    @JsonDeserialize(builder = FifoPesesImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface FifoPeses {
        Date data();
        String lot();
        long quantitat();
        String estante();
        Date dataEntrada();
        String etiquetaCaixa();
        String etiquetaPaler();
        String fabrica();
        String codiMagatzem();
        String embalatge();
    }
}

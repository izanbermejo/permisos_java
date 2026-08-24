package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirTrasabilitat;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.*;

@Component
public class ObtenirTrasabilitat implements IObtenirTrasabilitat {

    @Override
    public List<Map<String, TrasabilitatPesa>> get(KeyArticleClient articleClient) {
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
                        SELECT ARTINT, t.CLICOD, TM, DATA, ALBDAT, ALBARA, LOT, QUANT, EMPRESA, CLIMOV, ALBESPE, t.MAGCOD
                        FROM TRAZAT t
                        LEFT JOIN com.albcap a
                            ON t.albara = a.albcod AND t.empresa = a.empcod
                        WHERE ARTINT=? AND t.CLICOD=?
                        ORDER BY DATA DESC
                    """
            );
            statement.setString(1, articleClient.artint());
            statement.setString(2, articleClient.clicod());
            return statement;
        };

        ResultSetAction<List<Map<String, TrasabilitatPesa>>> rsAction = rs -> {
            List<Map<String, TrasabilitatPesa>> result = new ArrayList<>();
            while (rs.next()) {
                TrasabilitatPesa tras = TrasabilitatPesaImpl.builder()
                        .article(rs.getString("ARTINT"))
                        .clicod(rs.getString("CLICOD"))
                        .tipusMoviment(rs.getString("TM"))
                        .data(rs.getDate("DATA"))
                        .dataAlbara(Optional.ofNullable(rs.getDate("ALBDAT")))
                        .albara(rs.getString("ALBARA"))
                        .lot(rs.getString("LOT"))
                        .quantitat(rs.getLong("QUANT"))
                        .empresa(rs.getString("EMPRESA"))
                        .clientMoviment(rs.getString("CLIMOV"))
                        .albaraEspecial(rs.getString("ALBESPE"))
                        .codiMagatzem(rs.getString("MAGCOD"))
                        .build();

                Map<String, TrasabilitatPesa> map = new HashMap<>();
                map.put("res", tras); // clave que luego podrías usar como $F{res.data}, etc.
                result.add(map);
            }
            return result;
        };

        return new AdvantageDao().query(AdvantageDao.connectionMag, prep, rsAction);
    }


    @JsonDeserialize(builder = TrasabilitatPesaImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface TrasabilitatPesa{
        String article();
        String clicod();
        String tipusMoviment();
        Date data();
        Optional<Date> dataAlbara();
        String albara();
        String lot();
        long quantitat();
        String empresa();
        String clientMoviment();
        String albaraEspecial();
        String codiMagatzem();
    }
}

package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirAltreInfoClient;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirAltreInfoClient implements IObtenirAltreInfoClient {

    @Override
    public Optional<AltreInfoClient> get(String clicod) {

        PreparedStatementProvider prep = conn -> {

            var statement = conn.prepareStatement(
                    """
                    SELECT c.usuresp, r.repcod + ' ' + r.descrip as delegat, r.resp
                    FROM comundb.cli6 c
                    LEFT JOIN comundb.rep r ON r.repcod = c.clidel
                    WHERE c.clicod = ?
                    """
            );
            statement.setString(1, clicod);

            return statement;
        };

        ResultSetAction<Optional<AltreInfoClient>> rsAction = rs -> {
            if (rs.next()) {
                return Optional.of(
                        AltreInfoClientImpl.builder()
                                .delegat(rs.getString("delegat"))
                                .representant(rs.getString("resp"))
                                .respLogistica(rs.getString("usuresp"))
                                .build()
                );
            }
            return Optional.empty();
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    @JsonDeserialize(builder = AltreInfoClientImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface AltreInfoClient {

        String delegat();
        String representant();
        String respLogistica();
    }
}

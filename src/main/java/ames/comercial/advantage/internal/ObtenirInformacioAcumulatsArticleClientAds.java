package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirInformacioAcumulatsArticleClient;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class ObtenirInformacioAcumulatsArticleClientAds implements IObtenirInformacioAcumulatsArticleClient {

    @Override
    public InformacioAcumulatsArticleClient get(KeyArticleClient articleClient) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select stksal, albacum, datacum
						from comundb.artcli a
						LEFT JOIN dummy d ON a.artint = d.str1
						WHERE a.artint = ?
						AND a.clicod = ?
					""");
            statement.setString(1, articleClient.artint());
            statement.setString(2, articleClient.clicod());
            return statement;
        };
        ResultSetAction<InformacioAcumulatsArticleClient> rsAction = rs -> {
            if (rs.next()) {
                return InformacioAcumulatsArticleClientImpl.builder()
                        .quantitatAcumulada(rs.getLong("stksal"))
                        .numAlbara(MapperUtils.readOptionalString(rs, "albacum"))
                        .data(MapperUtils.readOptionalDate(rs, "datacum"))
                        .build();
            }
            return InformacioAcumulatsArticleClient.empty();
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    @JsonDeserialize(builder = InformacioAcumulatsArticleClientImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface InformacioAcumulatsArticleClient {
        long quantitatAcumulada();
        Optional<String> numAlbara();
        Optional<LocalDate> data();

        @Derived
        default long numAlbaraAsLong() {
            return numAlbara()
                    .filter(num -> !num.isBlank())
                    .map(Long::parseLong)
                    .orElse(0L);
        }

        static InformacioAcumulatsArticleClient empty() {
            return InformacioAcumulatsArticleClientImpl.builder()
                    .quantitatAcumulada(0)
                    .build();
        }
    }

}

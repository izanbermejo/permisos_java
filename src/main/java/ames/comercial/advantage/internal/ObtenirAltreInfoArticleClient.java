package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirAltreInfoArticleClient;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.shared.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Optional;

@Component
public class ObtenirAltreInfoArticleClient implements IObtenirAltreInfoArticleClient {

    @Override
    public Optional<AltreInfoArticleClient> get(String artcli) {

        PreparedStatementProvider prep = conn -> {
            // Separar el valor recibido
            String aclfab = artcli.substring(0, 7);
            String clicod = artcli.substring(7);

            var statement = conn.prepareStatement(
                    """
                    SELECT ac.artint, ac.clicod, c.usuresp, r.repcod + ' ' + r.descrip as delegat, r.resp, a.artpfin,
                           ac.resp2, pa.partida + ' ' + pa.nom as PartAranzelaria, ac.aclpre, ac.acldiv, ac.aclqpre, ac.aclper,
                           ac.aclpdat, ac.aclfenv, ac.cost, ac.aclind, COALESCE(ac.aclpreb, 0) as preuBase, ac.mat
                    FROM comundb.artcli ac
                    LEFT JOIN comundb.cli6 c ON c.clicod = ac.clicod
                    LEFT JOIN comundb.rep r ON r.repcod = c.clidel
                    LEFT JOIN comundb.art a ON a.artint = ac.artint
                    LEFT JOIN comundb.partara pa ON pa.codi = a.artara
                    WHERE ac.clicod = ?
                      AND ac.aclfab = ?
                    """
            );
            statement.setString(1, clicod);
            statement.setString(2, aclfab);

            return statement;
        };

        ResultSetAction<Optional<AltreInfoArticleClient>> rsAction = rs -> {
            if (rs.next()) {
                return Optional.of(
                        AltreInfoArticleClientImpl.builder()
                                .artInt(rs.getString("artint"))
                                .codiClient(rs.getString("clicod"))
                                .delegat(rs.getString("delegat"))
                                .representant(rs.getString("resp"))
                                .pesFinal(rs.getBigDecimal("artpfin"))
                                .respLogistica(rs.getString("usuresp"))
                                .partAranzel(rs.getString("PartAranzelaria"))
                                .planFabrica(rs.getString("resp2"))
                                .preu(rs.getBigDecimal("aclpre"))
                                .divisa(rs.getString("acldiv"))
                                .aclQPre(rs.getLong("aclqpre"))
                                .aclPer(rs.getString("aclper"))
                                .dataAplicacio(rs.getDate("aclpdat"))
                                .formaEnviament(rs.getString("aclfenv"))
                                .aclIndicador(rs.getString("aclind"))
                                .preuBase(rs.getBigDecimal("preuBase"))
                                .mat(rs.getString("mat"))
                                .cost(Optional.ofNullable(rs.getBigDecimal("cost")))
                                .build()
                );
            }
            return Optional.empty();
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    @JsonDeserialize(builder = AltreInfoArticleClientImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface AltreInfoArticleClient {

        String artInt();
        String codiClient();
        String delegat();
        String representant();
        BigDecimal pesFinal();
        String respLogistica();
        String partAranzel();
        String planFabrica();
        BigDecimal preu();
        String divisa();
        long aclQPre();
        String aclPer();
        Date dataAplicacio();
        String formaEnviament();
        String aclIndicador();
        BigDecimal preuBase();
        String mat();
        Optional<BigDecimal> cost();
    }
}

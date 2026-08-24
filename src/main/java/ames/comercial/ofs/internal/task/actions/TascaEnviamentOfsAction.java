package ames.comercial.ofs.internal.task.actions;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.ofs.internal.task.service.GenerarFitxerOf;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Numbers;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class TascaEnviamentOfsAction {

    @Autowired
    GenerarFitxerOf generarFitxerOf;
    @Autowired JdbcTemplate jdbc;

    private static final Map<String, String> mapPrefixFitxer = Map.of(
            "01", "al",
            "02", "si",
            "05", "tm",
            "06", "sm",
            "07", "re",
            "0A", "hu",
            "0B", "wu",
            "0D", "me");;

    public void executar() {
        // Obtenció de tots els articles
        var articles = obtenirArticlesClient();

        // Obtenció de totes les fàbriques que tenen OF per el dia
        var fabriques = fabriquesOf(LocalDate.now());

        // Per cada fàbrica s'obtenen les OF's i es genera el fitxer
        fabriques.forEach(codiFabrica -> {
            var numOfs = numerosOfFabrica(LocalDate.now(), codiFabrica);
            var prefix = mapPrefixFitxer.getOrDefault(codiFabrica, codiFabrica);
            generarFitxerOf.executar(articles, numOfs, prefix);
        });
    }

    private List<ArtcliEnviamentOf> obtenirArticlesClient() {
        PreparedStatementProvider prep = conn -> conn.prepareStatement("""
                SELECT ac.artint, ac.aclfab, ac.clicod, cli.clinom, cli8.clipro,
                ac.aclden, ac.aclref, art.artppre, art.artpfin, ac.aclpre, ac.acldiv,
                ac.aclnvt, ac.aclsts
                FROM comundb.artcli ac
                LEFT JOIN comundb.cli6 cli ON ac.clicod = cli.clicod
                LEFT JOIN comundb.cli8 cli8 ON cli.clicod = cli8.clicod AND cli.empcod = cli8.empcod
                LEFT JOIN comundb.art art ON ac.artint = art.artint
             """);
        ResultSetAction<List<ArtcliEnviamentOf>> rsAction = rs -> {
            var result = new ArrayList<ArtcliEnviamentOf>();
            while (rs.next()) {
                result.add(ArtcliEnviamentOfImpl.builder()
                        .artint(rs.getString("artint"))
                        .aclfab(rs.getString("aclfab"))
                        .clientCodi(Optional.ofNullable(rs.getString("clicod")).orElse(""))
                        .clientNom(Optional.ofNullable(rs.getString("clinom")).orElse(""))
                        .clientCodiProveidor(Optional.ofNullable(rs.getString("clipro")))
                        .denominacio(rs.getString("aclden"))
                        .referencia(rs.getString("aclref"))
                        .nivellTecnic(rs.getString("aclnvt"))
                        .pesPremsat(Optional.ofNullable(rs.getBigDecimal("artppre")).orElse(BigDecimal.ZERO))
                        .pesFinal(Optional.ofNullable(rs.getBigDecimal("artpfin")).orElse(BigDecimal.ZERO))
                        .preu(Optional.ofNullable(rs.getBigDecimal("aclpre")).orElse(BigDecimal.ZERO))
                        .divisa(rs.getString("acldiv"))
                        .aclsts(rs.getLong("aclsts"))
                        .build());
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private List<String> fabriquesOf(LocalDate data) {
        return jdbc.queryForList("""
                SELECT DISTINCT(fabrica)
                FROM ofs.ordre_fabricacio WHERE data_emissio = ?;
                """, String.class, data);
    }

    private List<Long> numerosOfFabrica(LocalDate data, String fabrica) {
        return jdbc.queryForList("""
                SELECT numero
                FROM ofs.ordre_fabricacio WHERE data_emissio = ? AND fabrica = ?
                """, Long.class, data, fabrica);
    }

    @JsonDeserialize(builder = ArtcliEnviamentOfImpl.class)
    @Style(typeImmutable = "*Impl")
    @Immutable
    public interface ArtcliEnviamentOf {
        String artint();
        String aclfab();
        String clientCodi();
        String clientNom();
        Optional<String> clientCodiProveidor();
        String denominacio();
        String referencia();
        String nivellTecnic();
        BigDecimal pesPremsat();
        BigDecimal pesFinal();
        BigDecimal preu();
        String divisa();
        long aclsts();    // Per normalitzat es l'stock mínim i per especial es tracta de l'stock de seguretat

        @Derived default KeyArticleClient articleClient() { return KeyArticleClient.of(artint(), clientCodi()); }

        @Derived default String codiArticleClient() { return aclfab() + clientCodi(); }

        @Derived default long pesPremsatCentigrams() { return pesPremsat().multiply(Numbers.decimal(100)).longValue(); }

        @Derived default long pesFinalCentigrams() { return pesFinal().multiply(Numbers.decimal(100)).longValue(); }

        @Derived default long preuMilesima() {return  preu().multiply(Numbers.decimal(1_000)).longValue(); }
    }

}

package ames.comercial.entrades.internal.application.service;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.advantage.internal.auxiliar.statements.MultipleInsertStatement;
import ames.comercial.entrades.internal.domain.EntradaMagatzem;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemEmbalatge;
import ames.comercial.shared.Pair;

import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class GeneraInsertsHisemba {

    EntradaMagatzem entradaMagatzem;

    public GeneraInsertsHisemba(EntradaMagatzem entradaMagatzem) {
        this.entradaMagatzem = entradaMagatzem;
    }

    public List<PreparedStatementProviderBatch> genera() {
        if (entradaMagatzem.embalatges().isEmpty())
            return List.of();

        List<PreparedStatementProviderBatch> result = new ArrayList<>();

        // Agrupació dels embalatges per codi d'element per obtenir la quantitat a historificar
        Map<String, Long> agrupacio = entradaMagatzem.embalatges()
                .stream()
                .collect(Collectors.groupingBy(EntradaMagatzemEmbalatge::codiElement,
                        Collectors.summingLong(EntradaMagatzemEmbalatge::quantitat)));

        // Obtenció del cliemba
        var clieEmba = obtenirCliemba(entradaMagatzem.embalatges().get(0).client());

        // Obtenció de les claus elements article que ja existeixen de MAGCOD=codiFabrica a MAGMOV=SF
        // per fer l'INSERT o UPDATE negatiu
        Map<Boolean, List<Pair<String, Long>>> mapMovimentsNegatius = agrupacio.entrySet()
                .stream()
                .collect(Collectors.partitioningBy(e ->
                        existeixMovimentHistoric(entradaMagatzem.fabrica(), "SF", e.getKey(), entradaMagatzem.dataEntrada(), entradaMagatzem.client()),
                        Collectors.mapping(
                                e -> new Pair<>(e.getKey(), e.getValue()),
                                Collectors.toList()
                        )
                ));
        // UPDATES negatius (tots aquells que ja existien en la taula hisemba per data, magatzems i codi element)
        var movimentsNegatiusExisteixen = mapMovimentsNegatius.get(true);
        if (!Objects.requireNonNullElse(movimentsNegatiusExisteixen, List.of()).isEmpty()) {
            result.add(updateHisEmba(movimentsNegatiusExisteixen, entradaMagatzem.fabrica(), "SF", entradaMagatzem.dataEntrada(), entradaMagatzem.client(), true));
        }
        // INSERTS negatius (tots aquells que no existien en la taula hisemba per data, magatzems i codi element)
        var movimentsNegatiusNoExisteixen = mapMovimentsNegatius.get(false);
        if (!Objects.requireNonNullElse(movimentsNegatiusNoExisteixen, List.of()).isEmpty()) {
            result.add(insertHisEmba(movimentsNegatiusNoExisteixen, clieEmba, entradaMagatzem.client(), entradaMagatzem.fabrica(), entradaMagatzem.dataEntrada(), true, "SF"));
        }

        // Obtenció de les claus elements article que ja existeixen de MAGCOD=SF a MAGMOV=codiFabrica
        // per fer l'INSERT o UPDATE positiu
        Map<Boolean, List<Pair<String, Long>>> mapMovimentsPositius = agrupacio.entrySet()
                .stream()
                .collect(Collectors.partitioningBy(e ->
                                existeixMovimentHistoric("SF", entradaMagatzem.fabrica(), e.getKey(), entradaMagatzem.dataEntrada(), entradaMagatzem.client()),
                        Collectors.mapping(
                                e -> new Pair<>(e.getKey(), e.getValue()),
                                Collectors.toList()
                        )
                ));
        // UPDATES positius (tots aquells que ja existien en la taula hisemba per data, magatzems i codi element)
        var movimentsPositiussExisteixen = mapMovimentsPositius.get(true);
        if (!Objects.requireNonNullElse(movimentsPositiussExisteixen, List.of()).isEmpty()) {
            result.add(updateHisEmba(movimentsPositiussExisteixen, "SF", entradaMagatzem.fabrica(), entradaMagatzem.dataEntrada(), entradaMagatzem.client(), false));
        }
        // INSERTS positius (tots aquells que no existien en la taula hisemba per data, magatzems i codi element)
        var movimentsPositiusNoExisteixen = mapMovimentsPositius.get(false);
        if (!Objects.requireNonNullElse(movimentsPositiusNoExisteixen, List.of()).isEmpty()) {
            result.add(insertHisEmba(movimentsPositiusNoExisteixen, clieEmba, entradaMagatzem.client(), "SF", entradaMagatzem.dataEntrada(), false, entradaMagatzem.fabrica()));
        }

        return result;
    }

    private String obtenirCliemba(String codiClient) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement("""
                SELECT cliemba
                FROM comundb.cli6 c
                LEFT JOIN dummy d on c.clicod = d.str1 
                WHERE clicod = ?
            """);
            statement.setString(1, codiClient);
            return statement;
        };
        ResultSetAction<String> rsAction = rs -> rs.next() ? rs.getString("cliemba") : "";
        return new AdvantageDao().query(prep, rsAction);
    }

    private boolean existeixMovimentHistoric(String codiFabrica, String magatzemMoviment, String codiElement,
                                             LocalDate dataEntrada, String codiClient) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement("""
                SELECT *
                FROM emb.hisemba
                WHERE tipmov = 'TM'
                AND data = ?
                AND magcod = ?
                AND magmov = ?
                AND codielem = ?
                AND clicod = ?
            """);
            statement.setDate(1, Date.valueOf(dataEntrada));
            statement.setString(2, codiFabrica);
            statement.setString(3, magatzemMoviment);
            statement.setString(4, codiElement);
            statement.setString(5, codiClient);
            return statement;
        };
        return new AdvantageDao().query(AdvantageDao.connectionMag, prep, ResultSet::next);
    }

    private PreparedStatementProviderBatch insertHisEmba(List<Pair<String, Long>> elementsQuantitat, String cliemba,
                                                         String clicod, String magcod, LocalDate data, boolean quantitatNegativa,
                                                         String magmov) {
        return conn -> new MultipleInsertStatement<Pair<String, Long>>(conn, "emb.hisemba")
                .add("codielem", Pair::first)
                .add("cliemba", d-> cliemba)
                .add("clicod", d-> clicod)
                .add("magcod", d-> magcod)
                .add("data", d-> data)
                .add("tipmov", d-> "TM")
                .add("quant", d-> quantitatNegativa ? -d.second() : d.second())
                .add("magmov", d-> magmov)
                .add("usureg", d -> "ENTRADES")
                .add("datareg", d -> LocalDate.now())
                .build(elementsQuantitat);
    }

    private PreparedStatementProviderBatch updateHisEmba(List<Pair<String, Long>> elementsQuantitat, String codiFabrica, String magatzemMoviment,
                                                         LocalDate dataEntrada, String codiClient, boolean quantitatNegativa) {
        return conn -> {
            var query = """
                UPDATE emb.hisemba SET quant =  quant + ?
                WHERE tipmov = 'TM'
                AND data = ?
                AND magcod = ?
                AND magmov = ?
                AND codielem = ?
                AND clicod = ?
                """;
            var prep = conn.prepareStatement(query);
            for (var d : elementsQuantitat) {
                prep.setLong(1, quantitatNegativa ? -d.second() : d.second());
                prep.setDate(2, Date.valueOf(dataEntrada));
                prep.setString(3, codiFabrica);
                prep.setString(4, magatzemMoviment);
                prep.setString(5, d.first());
                prep.setString(6, codiClient);
                prep.addBatch();
            }
            return prep;
        };
    }

}

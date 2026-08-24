package ames.comercial.entrades.internal.application.service;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.advantage.internal.auxiliar.statements.MultipleInsertStatement;
import ames.comercial.entrades.internal.domain.EntradaMagatzem;
import ames.comercial.entrades.internal.domain.EntradaMagatzem.EntradaMagatzemDetall;
import ames.comercial.shared.Pair;

import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneraInsertsTrazat {

    EntradaMagatzem entradaMagatzem;
    String artint;
    String empresa;

    public GeneraInsertsTrazat(EntradaMagatzem entradaMagatzem, String artint, String empresa) {
        this.entradaMagatzem = entradaMagatzem;
        this.artint = artint;
        this.empresa = empresa;
    }

    public List<PreparedStatementProviderBatch> genera() {
        // Obtenció dels acumulats de l'entrada a processar
        var acumulats = acumulatPerLots(entradaMagatzem);
        // Creació de Map per als lots existents i pels no existents
        // comprovant en el TRAZAT si hi ha una entrada per aquest lot
        Map<String, Long> lotsExistents = new HashMap<>();
        Map<String, Long> lotsNoExistents = new HashMap<>();
        acumulats.forEach((lot,quantitat) -> {
            if (existeixTrazatPerLot(lot)) {
                lotsExistents.put(lot, quantitat);
            } else {
                lotsNoExistents.put(lot, quantitat);
            }
        });
        // Creació dels INSERT per als lots no existents i UPDATE pels existents
        var optInserts = insertsTrazat(lotsNoExistents);
        var optUpdates = updateTrazat(lotsExistents);
        return Stream.concat(optInserts.stream(), optUpdates.stream()).toList();
    }

    private Optional<PreparedStatementProviderBatch> insertsTrazat(Map<String, Long> lotsNoExistents) {
        if (lotsNoExistents.isEmpty())
            return Optional.empty();
        return Optional.of(conn -> new MultipleInsertStatement<String>(conn, "trazat")
                .add("magcod", d -> entradaMagatzem.magatzem())
                .add("art", d -> entradaMagatzem.articleFabrica())
                .add("clicod", d -> entradaMagatzem.client())
                .add("climov", d -> entradaMagatzem.client())
                .add("data", d -> entradaMagatzem.dataEntrada())
                .add("lot", d -> d)
                .add("tm", d -> "EA")
                .add("quant", lotsNoExistents::get)
                .add("empresa", d -> empresa)
                .add("artint", d -> artint)
                .add("diareg", d -> LocalDate.now())
                .build(lotsNoExistents.keySet()));
    }

    private Optional<PreparedStatementProviderBatch> updateTrazat(Map<String, Long> lotsExistents) {
        if (lotsExistents.isEmpty())
            return Optional.empty();
        // Es passa el Map a una llista de parella String i Long
        var listUpdates = lotsExistents.entrySet()
                .stream()
                .map((k) -> new Pair<>(k.getKey(), k.getValue()))
                .toList();
        return Optional.of(conn -> {
            var query = """
                UPDATE trazat SET quant =  quant + ?
                where magcod = ?
                AND art = ?
                AND clicod = ?
                AND climov = ?
                AND tm = 'EA'
                AND lot = ?
                AND data = ?
                """;
            var prep = conn.prepareStatement(query);
            for (var d : listUpdates) {
                prep.setLong(1, d.second());
                prep.setString(2, entradaMagatzem.magatzem());
                prep.setString(3, entradaMagatzem.articleFabrica());
                prep.setString(4, entradaMagatzem.client());
                prep.setString(5, entradaMagatzem.client());
                prep.setString(6, d.first());
                prep.setDate(7, Date.valueOf(entradaMagatzem.dataEntrada()));
                prep.addBatch();
            }
            return prep;
        });
    }

    private Map<String, Long> acumulatPerLots (EntradaMagatzem entradaMagatzem) {
        if (entradaMagatzem.isPaletHomogeni()) {
            // En cas que sigui un palet es fa un acumulat de totes les caixes
            // processades correctament per lot i quantitat
            return entradaMagatzem.detalls().stream()
                    .filter(d -> d.error().isEmpty())
                    .collect(Collectors.groupingBy(EntradaMagatzemDetall::lot, Collectors.summingLong(EntradaMagatzemDetall::quantitat)));
        }
        // En cas que sigui una caixa si el procés no ha donat error
        // només genera un TRAZAT amb el lot i la quantitat indicats
        return entradaMagatzem.error().isPresent()
                ? Map.of()
                : Map.of(entradaMagatzem.lot(), entradaMagatzem.quantitat());
    }

    private boolean existeixTrazatPerLot(String lot) {
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select TOP 1 *
						from trazat
						where magcod = ?
						    AND art = ?
						    AND clicod = ?
						    AND climov = ?
						    AND tm = 'EA'
						    AND lot = ?
						    AND data = ?
					""");
            statement.setString(1, entradaMagatzem.magatzem());
            statement.setString(2, entradaMagatzem.articleFabrica());
            statement.setString(3, entradaMagatzem.client());
            statement.setString(4, entradaMagatzem.client());
            statement.setString(5, lot);
            statement.setDate(6, Date.valueOf(entradaMagatzem.dataEntrada()));
            return statement;
        };
        AdvantageDao.ResultSetAction<Boolean> rsAction = ResultSet::next;
        return new AdvantageDao().query(AdvantageDao.connectionMag, prep, rsAction);
    }

}

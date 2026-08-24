package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.Divisa;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ObtenirCanviDivisa {

    public BigDecimal get(Divisa divisaOrigen, Divisa divisaDesti, LocalDate data) {
        String columna = "CANVI" + data.getDayOfMonth();
        String query = String.format("""
                SELECT %s
                FROM comundb.div
                WHERE anymes = ? AND div = ? AND base = ?
                """, columna);
        PreparedStatementProvider statement = connection -> {
            var prep = connection.prepareStatement(query);
            prep.setString(1, DateTimeFormatter.ofPattern("yyyyMM").format(data));
            prep.setString(2, divisaOrigen.base().symbol());
            prep.setString(3, divisaDesti.base().symbol());
            return prep;
        };
        ResultSetAction<BigDecimal> rsAction = rs -> {
            if (rs.next())
                return rs.getBigDecimal(columna);
            throw new AppException(String.format("Canvi divisa de %s a %s no trobat per %s", divisaOrigen.base().symbol(), divisaDesti.base().symbol(), data));
        };
        return new AdvantageDao().query(statement, rsAction);
    }

    public Map<LocalDate, BigDecimal> get(Divisa divisaOrigen, Divisa divisaDesti, LocalDate desde, LocalDate fins) {
        // Es genera la llista d'anys mesos (YYYYMM) neceessaris perl camp anymes de la taula
        var anyMesos = generaAnyMes(desde, fins);
        String inClause = anyMesos.stream()
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(", "));
        String query = "SELECT * FROM comundb.div WHERE div = ? AND base = ? AND anymes IN (" + inClause + ")";
        PreparedStatementProvider statement = connection -> {
            var prep = connection.prepareStatement(query);
            prep.setString(1, divisaOrigen.base().symbol());
            prep.setString(2, divisaDesti.base().symbol());
            return prep;
        };
        ResultSetAction<Map<LocalDate, BigDecimal>> rsAction = rs -> {
            Map<LocalDate, BigDecimal> result = new HashMap<>();
            while (rs.next()) {
                String anyMes = rs.getString("anymes");
                YearMonth ym = YearMonth.of(
                        Integer.parseInt(anyMes.substring(0, 4)),
                        Integer.parseInt(anyMes.substring(4, 6))
                );
                for (int day = 1; day <= 31; day++) {
                    if (day > ym.lengthOfMonth()) continue;
                    String columnName = "CANVI" + day;
                    BigDecimal rate = rs.getBigDecimal(columnName);
                    LocalDate date = ym.atDay(day);
                    result.put(date, rate);
                }
            }
            return result;
        };
        return new AdvantageDao().query(statement, rsAction);
    }

    private List<String> generaAnyMes(LocalDate from, LocalDate to) {
        List<String> result = new ArrayList<>();
        YearMonth current = YearMonth.from(from);
        YearMonth end = YearMonth.from(to);

        while (!current.isAfter(end)) {
            result.add(current.toString().replace("-", "")); // e.g., "202405"
            current = current.plusMonths(1);
        }

        return result;
    }

}

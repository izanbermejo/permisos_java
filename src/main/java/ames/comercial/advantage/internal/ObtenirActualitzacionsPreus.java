package ames.comercial.advantage.internal;

import ames.comercial.shared.Divisa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ObtenirActualitzacionsPreus {

    public List<ActualitzacioPreu> get() {
        AdvantageDao.PreparedStatementProvider prep = conn -> conn.prepareStatement("""
                select *
                from actpre a
                left join dummy d ON a.artint = d.str1 
                where actual is null or actual = ''
            """);
        AdvantageDao.ResultSetAction<List<ActualitzacioPreu>> rsAction = rs -> {
            var result = new ArrayList<ActualitzacioPreu>();
            while (rs.next()) {
                result.add(new ActualitzacioPreu(
                        rs.getString("artint"),
                        rs.getString("clicod"),
                        rs.getBigDecimal("noupre"),
                        Divisa.getBySymbol(rs.getString("divisa"))));
            }
            return result;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    public record ActualitzacioPreu(String artint, String clicod, BigDecimal preu, Divisa divisa) {}

}

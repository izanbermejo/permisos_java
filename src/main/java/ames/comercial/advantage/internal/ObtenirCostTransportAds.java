package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirCostTransportAds;
import com.google.common.collect.TreeRangeMap;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

import static ames.comercial.shared.Numbers.decimal;
import static com.google.common.collect.Range.*;

@Component
public class ObtenirCostTransportAds implements IObtenirCostTransportAds {

    @Override
    public Optional<TreeRangeMap<BigDecimal, BigDecimal>> get(String pais) {
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    """
						select *
						from costra c
						left join dummy d on c.codpai = d.str1
						WHERE codpai = ?
					""");
            statement.setString(1, pais);
            return statement;
        };
        AdvantageDao.ResultSetAction<Optional<TreeRangeMap<BigDecimal, BigDecimal>>> rsAction = rs -> {
            if (rs.next()) {
                TreeRangeMap<BigDecimal, BigDecimal> rang = TreeRangeMap.create();
                rang.put(lessThan(decimal(10)), rs.getBigDecimal("r0010"));
                rang.put(closedOpen(decimal(10), decimal(20)), rs.getBigDecimal("r1020"));
                rang.put(closedOpen(decimal(20), decimal(30)), rs.getBigDecimal("r2030"));
                rang.put(closedOpen(decimal(30), decimal(40)), rs.getBigDecimal("r3040"));
                rang.put(closedOpen(decimal(40), decimal(50)), rs.getBigDecimal("r4050"));
                rang.put(closedOpen(decimal(50), decimal(100)), rs.getBigDecimal("r50100"));
                rang.put(closedOpen(decimal(100), decimal(250)), rs.getBigDecimal("r100250"));
                rang.put(closedOpen(decimal(250), decimal(500)), rs.getBigDecimal("r250500"));
                rang.put(closedOpen(decimal(500), decimal(750)), rs.getBigDecimal("r500750"));
                rang.put(closedOpen(decimal(750), decimal(1000)), rs.getBigDecimal("r7501000"));
                rang.put(atLeast(decimal(1000)), rs.getBigDecimal("r100099999"));
                return Optional.of(rang);
            }
            return Optional.empty();
        };
        return new AdvantageDao().query(prep, rsAction);
    }

}

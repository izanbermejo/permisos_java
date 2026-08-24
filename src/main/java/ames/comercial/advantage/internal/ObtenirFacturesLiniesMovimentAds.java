package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.advantage.internal.response.QueryFacturesLiniesMovimentResponse;
import ames.comercial.advantage.internal.response.QueryFacturesLiniesMovimentResponseImpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ObtenirFacturesLiniesMovimentAds {

    public List<QueryFacturesLiniesMovimentResponse> query(long albara, String empresa, String clicod, String artint) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement("""
                    SELECT l.fclart, l.empcod, l.fcctip, f.fccnum, l.fcllin, l.tiplin, l.matriu, f.codcli, l.fclcom, l.fclq, l.fclpre, l.fclimp, l.divimplin, l.fcldiv, l.fcldtonor, l.fclfrannt
                    FROM faclin l
                    LEFT JOIN faccap f ON f.empcod = l.empcod AND f.fccnum = l.fccnum
                    WHERE (l.fclalb = ? OR l.fclalb = ?)
                          AND l.empcod = ?
                          AND f.codcli = ?
                          AND l.fclart = ?
                    """);
            statement.setString(1, String.format("%07d", albara));
            statement.setString(2, String.valueOf(albara));
            statement.setString(3, empresa);
            statement.setString(4, clicod);
            statement.setString(5, artint);
            return statement;
        };
        ResultSetAction<List<QueryFacturesLiniesMovimentResponse>> rsAction = rs -> {
            List<QueryFacturesLiniesMovimentResponse> resultat = new ArrayList<>();
            while (rs.next()) {
                resultat.add(QueryFacturesLiniesMovimentResponseImpl.builder()
                        .albara(albara)
                        .artint(readSafe(rs.getString("fclart")))
                        .empcod(readSafe(rs.getString("empcod")))
                        .fcctip(readSafe(rs.getString("fcctip")))
                        .fccnum(readSafe(rs.getString("fccnum")))
                        .fcllin(rs.getLong("fcllin"))
                        .tiplin(readSafe(rs.getString("tiplin")))
                        .matriu(readSafe(rs.getString("matriu")))
                        .codcli(readSafe(rs.getString("codcli")))
                        .fclcom(readSafe(rs.getString("fclcom")))
                        .fclq(rs.getLong("fclq"))
                        .fclpre(readSafeBigDecimal(rs.getBigDecimal("fclpre")))
                        .fclimp(readSafeBigDecimal(rs.getBigDecimal("fclimp")))
                        .fcldtonor(readSafeBigDecimal(rs.getBigDecimal("fcldtonor")))
                        .divimplin(readSafe(rs.getString("divimplin")))
                        .fcldiv(readSafe(rs.getString("fcldiv")))
                        .fclfrannt(Optional.ofNullable(rs.getString("fclfrannt")).filter(s -> !s.isBlank()))
                        .build());
            }
            return resultat;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    private String readSafe(String value) {
        return value != null ? value : "";
    }

    private BigDecimal readSafeBigDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

}

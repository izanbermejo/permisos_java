package ames.comercial.advantage.internal.auxiliar.generators;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;

import java.math.BigDecimal;

public class ArtGenerator {

    public static PreparedStatementProviderBatch updatePesPremsat(String artint, BigDecimal pesPremsat) {
        return connection -> {
            var query = "UPDATE comundb.art SET artpre = ? WHERE artint = ?";
            var prep = connection.prepareStatement(query);
            prep.setBigDecimal(1, pesPremsat);
            prep.setString(2, artint);
            prep.addBatch();
            return prep;
        };
    }

    public static PreparedStatementProviderBatch updatePesFinal(String artint, BigDecimal pesFinal) {
        return connection -> {
            var query = "UPDATE comundb.art SET artpfin = ? WHERE artint = ?";
            var prep = connection.prepareStatement(query);
            prep.setBigDecimal(1, pesFinal);
            prep.setString(2, artint);
                prep.addBatch();
            return prep;
        };
    }

}

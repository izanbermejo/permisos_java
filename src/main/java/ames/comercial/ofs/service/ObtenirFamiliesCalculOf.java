package ames.comercial.ofs.service;

import ames.comercial.advantage.internal.AdvantageDao;
import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.comandes.service.Familia;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ObtenirFamiliesCalculOf {

    public List<Familia> executar() {
        PreparedStatementProvider statement = connection ->
                connection.prepareStatement("""
                    SELECT famnor
                    FROM setup
                """);
        ResultSetAction<List<Familia>> rsAction = rs -> {
            if (rs.next()) {
                var result = new ArrayList<Familia>();
                var strFamilies = rs.getString("famnor").trim();
                for (int i = 0; i < strFamilies.length(); i++ ) {
                    result.add(Familia.getByFamilia(String.valueOf(strFamilies.charAt(i))));
                }
                return result;
            }
            return List.of();
        };
        return new AdvantageDao().query(statement, rsAction);
    }

}

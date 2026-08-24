package ames.comercial.advantage.internal;

import ames.comercial.comandes.service.Familia;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ObtenirFamiliesAds implements IObtenirFamiliesAds {

    @Override
    public Map<String, Familia> query(Set<String> articles) {
        if (articles.isEmpty())
            return Map.of();
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    String.format("""
							select artint, artfam
							from comundb.art a
							left join dummy d on a.artint = d.str1
							where artint IN(%s);
							""", articles.stream().map(v -> "?").collect(Collectors.joining(", "))));
            int index = 1;
            for (var s : articles) {
                statement.setString(index++, s);
            }
            return statement;
        };
        AdvantageDao.ResultSetAction<Map<String, Familia>> rsAction = rs -> {
            Map<String, Familia> resultat = new HashMap<>();
            while (rs.next()) {
                resultat.put(rs.getString("artint"), Familia.getByFamilia(rs.getString("artfam")));
            }
            return resultat;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

}

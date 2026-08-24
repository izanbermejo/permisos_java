package ames.comercial.advantage.internal;

import ames.comercial.advantage.IObtenirClientEDIAds;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirClientEDIAds implements IObtenirClientEDIAds {

    @Override
    public Optional<ClientEDIAds> get(String codicli, String document, String buzonDeEntrada) {
        System.out.println("String codicli, String document, String buzonDeEntrada" +  codicli +"/" +document+"/" +buzonDeEntrada);
        // TODO Treure aquest if
        if (document.equals("DELFOR")) document = "DELF";
        else if (document.equals("DELJIT")) document = "DELJ";
        else if (document.equals("ORDERR")) document = "DELJ";
        else if (document.equals("ORDERS")) document = "DELJ";
        else if (document.equals("DELINS")) document = "DELI";
            else document = "";
        String finalDocument = document;
        AdvantageDao.PreparedStatementProvider prep = conn -> {
            if (buzonDeEntrada!=null && !buzonDeEntrada.equals("")) {
                var statement = conn.prepareStatement(
                        """
                                	select *
                                	from flagsedi f
                                	left join dummy d on f.codcli = d.str1
                                	WHERE codcli = ? and docum = ? and ediboxe= ?
                                """);
                statement.setString(1, codicli);
                statement.setString(2, finalDocument);
                statement.setString(3, buzonDeEntrada);

                return statement;
            }
            else {
                var statement = conn.prepareStatement(
                        """
                                	select *
                                	from flagsedi
                                	WHERE codcli = ? and docum = ?
                                """);
                statement.setString(1, codicli);
                statement.setString(2, finalDocument);

                return statement;
            }
        };
        AdvantageDao.ResultSetAction<Optional<ClientEDIAds>> rsAction = rs -> {
            if (rs.next()) {
                TipusLiniaComanda tipusLinia;
                if (rs.getString("ferori").equals("O"))
                    tipusLinia = TipusLiniaComanda.ORIENTATIU;
                else
                    tipusLinia = TipusLiniaComanda.FERM;
                return Optional.of(
                        new ClientEDIAds(
                                rs.getString("codcli"),
                                rs.getString("docum"),
                                rs.getString("nom"),
                                rs.getString("ediboxe"),
                                rs.getString("nad02"),
                                Optional.ofNullable(rs.getString("diessor")),
                                Optional.ofNullable(rs.getString("encoalb")),
                                rs.getInt("diesres"),
                                Optional.ofNullable(rs.getString("dosdat")),
                                Optional.ofNullable(tipusLinia),
                                Optional.ofNullable(rs.getString("editant")),
                                rs.getString("empcod"),
                                rs.getString("tipedi"),
                                rs.getString("diestall")
                        ));
            }
            return Optional.empty();
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    public record ClientEDIAds(String clicod, String docum, String nom, String ediboxe, String nad02,
                               Optional<String> diessor, Optional<String> encoalb, int diesres, Optional<String> dosdat,
                               Optional<TipusLiniaComanda> ferori, Optional<String> editant, String empcod, String tipedi, String diestall) {
    }
}
package ames.comercial.advantage.internal.auxiliar.generators;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.advantage.internal.auxiliar.statements.MultipleInsertStatement;
import ames.comercial.albarans.internal.domain.linia.InformacioComanda;
import ames.comercial.albarans.internal.domain.linia.LiniaAlbara;
import ames.comercial.server.RequestThread;

import java.util.List;

public class AlbLinGenerator {

    public static PreparedStatementProviderBatch insert(LiniaAlbara linia) {
        return insert(List.of(linia));
    }

    public static PreparedStatementProviderBatch insert(List<LiniaAlbara> linies) {
        return conn -> new MultipleInsertStatement<LiniaAlbara>(conn, "alblin")
                .add("empcod", l -> l.id().idAlbara().empresa())
                .add("albcod", l -> l.id().idAlbara().codiFormat())
                .add("alblin", l -> l.id().liniaFormat())
                .add("comcod", l -> l.infoComanda().flatMap(InformacioComanda::comanda).orElse(null))
                .add("acomsc", l -> l.infoComanda().map(InformacioComanda::comandaClient).orElse(""))
                .add("aprosc", l -> l.infoComanda().map(InformacioComanda::programa).orElse(""))
                .add("artint", l -> l.articleClient().artint())
                .add("clicod", l -> l.articleClient().clicod())
                .add("albqua", LiniaAlbara::quantitat)
                .add("albpre", l -> l.preu().valor())
                .add("albdiv", l -> l.preu().divisa().toString())
                .add("albopc", AlbLinGenerator::calculaAlbOpc)
                .add("diareg", l -> RequestThread.dateLocal())
                .add("usuari", l -> RequestThread.nomUsuari())
                .add("observ",l -> l.observacionsImpressio().orElse(""))
                .add("notes",l -> l.observacionsInternes().orElse(""))
                .add("penfac", LiniaAlbara::quantitatPendentFacturar)
                // TODO Informació de l'EDI (punto, aclgat, codiemba, csg3921)
                .add("pecaexpres", l -> "N") // TODO Cal informar-se si caldrà replicar-ho
                .add("qtypendent", LiniaAlbara::quantitatPendentConsumir)
                .build(linies);
    }

    private static String calculaAlbOpc(LiniaAlbara liniaAlbara) {
        if (liniaAlbara.comandaBlanca().isPresent())
            return "8"; // En cas que sigui CB es posa un 8 per facturar les mostres.
        if (liniaAlbara.isPreuFixat())
            return "2"; // En cas que sigui preu fixat es posa un 2
        return "0"; // En cas contrari es posa un 0
    }

    public static PreparedStatementProviderBatch delete(List<LiniaAlbara> linies) {
        return conn -> {
            var query = "DELETE FROM alblin WHERE empcod = ? AND albcod = ? AND alblin = ?";
            var prep = conn.prepareStatement(query);
            for (var l : linies) {
                prep.setString(1, l.id().idAlbara().empresa());
                prep.setString(2, l.id().idAlbara().codiFormat());
                prep.setString(3, l.id().liniaFormat());
                prep.addBatch();
            }
            return prep;
        };
    }

}

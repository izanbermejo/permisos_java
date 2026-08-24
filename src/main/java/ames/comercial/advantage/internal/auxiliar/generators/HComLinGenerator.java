package ames.comercial.advantage.internal.auxiliar.generators;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.advantage.internal.auxiliar.statements.MultipleInsertStatement;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.Pair;

import java.util.List;

public class HComLinGenerator {

    public static PreparedStatementProviderBatch insert(LiniaComanda linia)  {
        return insert(List.of(linia));
    }

    public static PreparedStatementProviderBatch insert(List<LiniaComanda> linies) {
        return conn -> new MultipleInsertStatement<LiniaComanda>(conn, "hcomlin")
                .add("comcod", LiniaComanda::codiFormat)
                .add("hcomlin", LiniaComanda::numeroFormat)
                .add("actual", l -> "S")
                .add("artint", l -> l.articleClient().artint())
                .add("clicod", l -> l.articleClient().clicod())
                .add("hcomqua", LiniaComanda::quantitat)
                .add("hcompre", l -> l.preu().valor())
                .add("hcomdiv", l -> l.preu().divisa().toString())
                .add("hcomdats", LiniaComanda::dataSolicitada)
                .add("hcomdatm", LiniaComanda::dataPrevistaSortida)
                .add("hcomdatp", l -> l.dataPrevistaSortidaInterna().orElse(l.dataPrevistaSortida()))
                .add("hcomdatc", l -> l.dataConfirmadaFabrica().orElse(null))
                .add("hcomtip", l -> l.tipus().clauAdvantage())
                .add("hcoment", HComLinGenerator::calculaComEnt)
                .add("hcomqser", LiniaComanda::quantitatServida)
                .add("hcomqres", LiniaComanda::quantitatReservada)
                .add("diareg", l -> RequestThread.dateLocal())
                .add("usuari", l -> RequestThread.nomUsuari())
                .build(linies);
    }

    static int calculaComEnt(LiniaComanda l) {
        if (l.comandaBlanca().isPresent()) return 8;
        if (l.isPreuFixat()) return 2;
        return 0;
    }

    public static PreparedStatementProviderBatch updateActualN(List<LiniaComanda> linies) {
        return conn -> {
            var query = "UPDATE hcomlin SET actual = 'N' WHERE comcod = ? AND hcomlin = ?";
            var prep = conn.prepareStatement(query);
            for (var l : linies) {
                prep.setString(1, l.codiFormat());
                prep.setString(2, l.numeroFormat());
                prep.addBatch();
            }
            return prep;
        };
    }

    public static PreparedStatementProviderBatch updateDesferServida(List<Pair<LiniaComanda, Long>> linies) {
        return conn -> {
            var query = "UPDATE hcomlin SET hcomqser = hcomqser - ?, hcomqres = hcomqres + ?, serv='N' WHERE comcod = ? AND hcomlin = ? AND actual = 'S'";
            var prep = conn.prepareStatement(query);
            for (var l : linies) {
                prep.setLong(1, l.second());
                prep.setLong(2, l.first().articleClient().clicod().equals("000000") ? l.second() : 0);
                prep.setString(3, l.first().codiFormat());
                prep.setString(4, l.first().numeroFormat());
                prep.addBatch();
            }
            return prep;
        };
    }

    public static PreparedStatementProviderBatch updateServida(List<Pair<LiniaComanda, Long>> linies) {
        return conn -> {
            var query = "UPDATE hcomlin SET hcomqser = hcomqser + ?, hcomqres = hcomqres - ?, serv=? WHERE comcod = ? AND hcomlin = ? AND actual = 'S'";
            var prep = conn.prepareStatement(query);
            for (var l : linies) {
                prep.setLong(1, l.second());
                prep.setLong(2, l.first().articleClient().clicod().equals("000000") ? l.second() : 0);
                prep.setString(3, l.first().servida() ? "S" : "N");
                prep.setString(4, l.first().codiFormat());
                prep.setString(5, l.first().numeroFormat());
                prep.addBatch();
            }
            return prep;
        };
    }

    public static PreparedStatementProviderBatch copyComentarisFromPreviousVersion(List<LiniaComanda> linies) {
        return conn -> {
            var query = """
                UPDATE hcomlin SET
                    commemo = (SELECT TOP 1 commemo FROM hcomlin h WHERE h.comcod = hcomlin.comcod AND h.hcomlin = hcomlin.hcomlin AND h.actual = 'N'),
                    comseg  = (SELECT TOP 1 comseg  FROM hcomlin h WHERE h.comcod = hcomlin.comcod AND h.hcomlin = hcomlin.hcomlin AND h.actual = 'N')
                WHERE comcod = ? AND hcomlin = ?
                """;
            var prep = conn.prepareStatement(query);
            for (var l : linies) {
                prep.setString(1, l.codiFormat());
                prep.setString(2, l.numeroFormat());
                prep.addBatch();
            }
            return prep;
        };
    }

    public static PreparedStatementProviderBatch updateComentariIntern(List<Pair<KeyLiniaComanda,String>> linies) {
        return conn -> {
            var query = "UPDATE hcomlin SET commemo = ? WHERE comcod = ? AND hcomlin = ?";
            var prep = conn.prepareStatement(query);
            for (var l : linies) {
                prep.setString(1, l.second());
                prep.setString(2, l.first().comandaFormat());
                prep.setString(3, l.first().numeroFormat());
                prep.addBatch();
            }
            return prep;
        };
    }

    public static PreparedStatementProviderBatch updateComentariExtern(List<Pair<KeyLiniaComanda,String>> linies) {
        return conn -> {
            var query = "UPDATE hcomlin SET comseg = ? WHERE comcod = ? AND hcomlin = ?";
            var prep = conn.prepareStatement(query);
            for (var l : linies) {
                prep.setString(1, l.second());
                prep.setString(2, l.first().comandaFormat());
                prep.setString(3, l.first().numeroFormat());
                prep.addBatch();
            }
            return prep;
        };
    }

}

package ames.comercial.advantage.internal.auxiliar.generators;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProviderBatch;
import ames.comercial.advantage.internal.auxiliar.statements.MultipleInsertStatement;
import ames.comercial.albarans.internal.domain.albara.*;
import ames.comercial.albarans.internal.domain.albara.InformacioMagatzem.InformacioPalet;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.Adresa;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AlbCapGenerator {

    public static PreparedStatementProviderBatch delete(List<Albara> albarans) {
        return conn -> {
            var statement = conn.prepareStatement("DELETE FROM albcap WHERE empcod = ? AND albcod = ?");
            for (Albara a : albarans) {
                statement.setString(1, a.id().empresa());
                statement.setString(2, a.id().codiFormat());
                statement.addBatch();
            }
            return statement;
        };
    }

    public static PreparedStatementProviderBatch updateFacturat(List<Albara> albarans) {
        return conn -> {
            var statement = conn.prepareStatement("UPDATE albcap SET albfac1 = ? WHERE empcod = ? AND albcod = ?");
            for (Albara a : albarans) {
                statement.setString(1, a.isFacturat() ? "S" : "N");
                statement.setString(2, a.id().empresa());
                statement.setString(3, a.id().codiFormat());
                statement.addBatch();
            }
            return statement;
        };
    }

    public static PreparedStatementProviderBatch updateFacturacioAutomatica(List<Albara> albarans) {
        return conn -> {
            var statement = conn.prepareStatement("UPDATE albcap SET factauto = ? WHERE empcod = ? AND albcod = ?");
            for (Albara a : albarans) {
                statement.setString(1, a.isFacturacioAutomatica() ? "S" : "N");
                statement.setString(2, a.id().empresa());
                statement.setString(3, a.id().codiFormat());
                statement.addBatch();
            }
            return statement;
        };
    }

    public static PreparedStatementProviderBatch insert(Albara comanda){
        return insert(List.of(comanda));
    }

    public static PreparedStatementProviderBatch insert(List<Albara> albarans) {
        return conn -> new MultipleInsertStatement<Albara>(conn, "albcap")
                .add("empcod", l -> l.id().empresa())
                .add("albcod", l -> l.id().codiFormat())
                .add("albdat", Albara::data)
                .add("albnom", l -> l.adresa().destinatari())
                .add("albdre", l -> l.adresa().adresa())
                .add("albpob", l -> l.adresa().poblacio())
                .add("codpos", l -> l.adresa().codiPostal())
                .add("albpai", l -> l.adresa().pais())
                .add("clicod", l -> l.client().orElse(null))
                .add("albtra", l -> l.informacioEnviament().transportista().orElse(""))
                .add("albenv", l -> l.informacioEnviament().comenv())
                .add("albpro", l -> l.numeroProveidor().orElse(null))
                .add("magcod", Albara::magatzem)
                .add("magcodr", l -> l.informacioTraspas().map(InformacioTraspas::magatzemReceptor).orElse(null))
                .add("albesp", l -> l.numeroAlbaraEspecial().orElse(null))
                .add("diareg", l -> RequestThread.dateLocal())
                .add("albserv", l -> l.informacioMagatzem().isServit() ? "S" : "N")
                .add("albenserv", l -> l.informacioMagatzem().isEnServei() ? "S" : "N")
                .add("albtancat", l -> l.isTancat() ? "S" : "N")
                .add("albfac1", l -> l.isFacturat() ? "S" : "N")
                .add("usuari", l -> RequestThread.nomUsuari())
                .add("observ", l -> l.observacionsImpressio().orElse(null))
                .add("notes", l -> l.observacionsInternes().orElse(null))
                .add("pesbru", l -> l.informacioMagatzem().pesBrut().orElse(null))
                .add("pesbru", l -> l.informacioMagatzem().bultos().orElse(null))
                .add("nomb", l -> l.adresaBroker().map(Adresa::destinatari).orElse(null))
                .add("dreb", l -> l.adresaBroker().map(Adresa::adresa).orElse(null))
                .add("pobb", l -> l.adresaBroker().map(Adresa::poblacio).orElse(null))
                .add("posb", l -> l.adresaBroker().map(Adresa::codiPostal).orElse(null))
                .add("paib", l -> l.adresaBroker().map(Adresa::pais).orElse(null))
                .add("nomf", l -> l.adresaFacturaProforma().map(Adresa::destinatari).orElse(null))
                .add("dref", l -> l.adresaFacturaProforma().map(Adresa::adresa).orElse(null))
                .add("pobf", l -> l.adresaFacturaProforma().map(Adresa::poblacio).orElse(null))
                .add("posf", l -> l.adresaFacturaProforma().map(Adresa::codiPostal).orElse(null))
                .add("paif", l -> l.adresaFacturaProforma().map(Adresa::pais).orElse(null))
                .add("albfax", Albara::isEnviatEmail)
                .add("factauto", Albara::isFacturacioAutomatica)
                .add("albnor", Albara::isNormalitzats)
                .add("matricula", l -> l.informacioMagatzem().matricula().orElse(null))
                .add("dataprev", l -> l.informacioMagatzem().dataPrevista().map(LocalDateTime::toLocalDate).orElse(null))
                .add("horaprev", l -> l.informacioMagatzem().dataPrevista().map(dt -> dt.format(DateTimeFormatter.ofPattern("HH:mm"))).orElse(null))
                .add("dataenv", l -> l.informacioMagatzem().dataEnviament().map(LocalDateTime::toLocalDate).orElse(null))
                .add("horaenv", l -> l.informacioMagatzem().dataEnviament().map(dt -> dt.format(DateTimeFormatter.ofPattern("HH:mm"))).orElse(null))
                // TODO AVINUM (ho informa el magtzem -> Tenir en comtpe per l'API).add("avinum", l -> l.informacioEdi().map(Informa)
                .add("punto", l -> l.informacioEdi().map(InformacioEdi::punto).orElse(null))
                .add("aclgat", l -> l.informacioEdi().map(InformacioEdi::aclgat).orElse(null))
                .add("csg3921", l -> l.informacioEdi().map(InformacioEdi::csg3921).orElse(null))
                .add("albenprep", l -> l.informacioMagatzem().isEnPreparacio())
                .add("avienviat", l -> l.informacioMagatzem().isAvisEnviat())
                .add("zontra", l -> l.informacioEnviament().zonaTransport().orElse(null))
                .add("tecostra", l -> l.costTransport().isPresent())
                .add("costra", l -> l.costTransport().map(CostTransport::imp).orElse(null))
                .add("entregat", l -> l.informacioMagatzem().isEntregat())
                .add("pagatports", Albara::isCalPagarPorts)
                .add("facttrans", l -> l.numeroFacturaTransport().orElse(null))
                .add("desti", l -> l.destiAlternatiu().orElse(null))
                .add("albexpres", l -> l.costEnviamentExpress().isPresent())
                .add("costexpres", l -> l.costEnviamentExpress().map(CostEnviamentExpress::imp).orElse(null))
                .add("numalbmix", l -> l.informacioMagatzem().numAlbaraMix().orElse(null))
                .add("numpal1", l -> l.informacioMagatzem().paletsTipus1().map(InformacioPalet::numero).orElse(null))
                .add("numpal2", l -> l.informacioMagatzem().paletsTipus2().map(InformacioPalet::numero).orElse(null))
                .add("numpal3", l -> l.informacioMagatzem().paletsTipus3().map(InformacioPalet::numero).orElse(null))
                .add("numbox", l -> l.numeroCaixes().orElse(null))
                .add("novalorat", Albara::isNoValorat)
                .add("empcodr", l -> l.informacioTraspas().map(InformacioTraspas::empresaReceptora).orElse(null))
                .add("altpalet1", l -> l.informacioMagatzem().paletsTipus1().map(InformacioPalet::alsada).orElse(null))
                .add("altpalet2", l -> l.informacioMagatzem().paletsTipus2().map(InformacioPalet::alsada).orElse(null))
                .add("altpalet3", l -> l.informacioMagatzem().paletsTipus3().map(InformacioPalet::alsada).orElse(null))
                .add("raoexpres", l -> l.costEnviamentExpress().map(CostEnviamentExpress::motiu).orElse(null))
                .add("trasabon", l -> l.informacioTraspas().map(InformacioTraspas::isTraspasAbonable).orElse(null))
                .add("incitrans", l -> l.incidenciaTransport().isPresent() ? "S" : "N")
                .add("raoinci", l -> l.incidenciaTransport().orElse(null))
                .add("costmoq", l -> l.costMoq().orElse(null))
                .add("reftransp", l -> l.referenciaTransport().orElse(null))
                .add("altbox", l -> l.alsadaCaixes().orElse(null))
                .add("envhis", Albara::isEnviatHisenda)
                .add("costlogi", l -> l.costLogistic().isPresent() ? "S" : "N")
                .add("implogi", l -> l.costLogistic().map(CostLogistic::imp).orElse(null))
                .add("textlogi", l -> l.costLogistic().map(CostLogistic::comentaris).orElse(null))
                .add("notaprof", l -> l.observacionsProforma().orElse(null))
                .add("mrnnum", l -> l.informacioEdi().map(InformacioEdi::mrnNum).orElse(null))
                .add("mrndata", l -> l.informacioEdi().map(InformacioEdi::mrnData).orElse(null))
                .add("mrntype", l -> l.informacioEdi().map(InformacioEdi::mrnType).orElse(null))
                .add("albexpres", Albara::isUrgent)
                .build(albarans);
    }

}

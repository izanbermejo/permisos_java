package ames.comercial.advantage.internal;

import ames.comercial.advantage.internal.AdvantageDao.PreparedStatementProvider;
import ames.comercial.advantage.internal.AdvantageDao.ResultSetAction;
import ames.comercial.advantage.internal.response.QueryAlbaransFacturesByArticleAndClientResponse;
import ames.comercial.advantage.internal.response.QueryAlbaransFacturesByArticleAndClientResponseImpl;
import ames.comercial.server.RequestThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ObtenirAlbaransFacturesByArticleAndClientAds {

    //El cambiem a 200 200250416
    static long diesAntelacioDEF = 400;

    static final Logger log = LogManager.getLogger(ObtenirAlbaransFacturesByArticleAndClientAds.class.getName());

    public List<QueryAlbaransFacturesByArticleAndClientResponse> queryAlbara(String artint, String empresa, String clicod, String ultimAlabaraRebut, boolean plataforma, String dataAcumulat, String estocAcumulat, String albaraAcumulat, String enviamentClient) {
        int histip;
        if (dataAcumulat == null) dataAcumulat = "1899-01-01";

        Date dataAcumulatDate = Date.valueOf(dataAcumulat);

        long diesAntelacio;

        LocalDate data = dataAcumulatDate.toLocalDate(); // convertir java.sql.Date a LocalDate

        LocalDate avui = RequestThread.dateLocal();

        if (data.isBefore(avui.minusDays(diesAntelacioDEF))) {
            diesAntelacio = diesAntelacioDEF;
//            log.trace("La fecha es posterior a hoy menos " + diesAntelacio + " días.");
        } else {
            diesAntelacio = ChronoUnit.DAYS.between(data, avui);
//            log.trace("La fecha es anterior o igual." + diesAntelacio);
        }

        if (plataforma) histip = 4;
        else
            histip = 2;
        PreparedStatementProvider prep = conn -> {
            String whereHisCom = "";
            if (!plataforma) whereHisCom = "and hiscom <> ''";
            // per obtenir la factura fer un LEFTJOIN amb faclin on fclalb=h.hisalb
            var statement = conn.prepareStatement(
//                    "SELECT h.hisalb, MAX(fl.fccnum) as factura, MAX(h.hisdia) AS hisdia, MAX(a.albserv) as servida, MAX(a.albenv) AS incoterm, SUM(h.hisqua) AS hisqua " +
                    "SELECT h.hisalb, MAX(a.albtra) codi_transportista, MAX(h.hisdia) AS hisdia, MAX(a.ENTREGAT) as servida, MAX(a.albenv) AS incoterm, SUM(h.hisqua) AS hisqua ,MAX(h.empcod) empresa " +
                            "from his h " +
                            "LEFT JOIN albcap a ON h.hisalb = a.albcod and h.empcod=a.empcod " +
//                            "LEFT JOIN faclin fl ON h.hisalb = fl.FCLALB and h.empcod=fl.empcod and fl.fclart=h.artint " +
                            "where h.artint = ? and h.hisalb is not null AND h.histip=" + histip + " and h.clicod = ? " + whereHisCom + " and hisqua>0 " +
                            "and HISDIA > (CURDATE() - " + diesAntelacio + ")" +
//						"and HISDIA BETWEEN (CURDATE() - " + diesAntelacio + ") AND CURDATE()" +
                            "GROUP BY hisalb " +
                            "order by hisdia desc");

            statement.setString(1, artint);
            statement.setString(2, clicod);
            return statement;
        };
        String finalDataAcumulat = dataAcumulat;
        ResultSetAction<List<QueryAlbaransFacturesByArticleAndClientResponse>> rsAction = rs -> {
            List<QueryAlbaransFacturesByArticleAndClientResponse> resultat = new ArrayList<QueryAlbaransFacturesByArticleAndClientResponse>();

            Long transit = 0L;
            Long estocAcumulatLong = 0L;

            if (estocAcumulat != null)
                estocAcumulatLong = Long.parseLong(estocAcumulat);
//			if (dataAcumulat!=null)
//				dataAcumulat= "1899-01-01";
            Boolean tallTransit = false;
            Boolean tallAcumulat = false;
//			if (albaraAcumulat!=null) {
//				int index=1;
            while (rs.next()) {// && !tallAcumulat) {
                transit = transit + rs.getLong("hisqua");
                QueryAlbaransFacturesByArticleAndClientResponseImpl.Builder albaraBuilder = QueryAlbaransFacturesByArticleAndClientResponseImpl.builder();
                albaraBuilder.albara(rs.getString("hisalb"));
//                if (rs.getString("factura")!=null)
//                    albaraBuilder.factura(rs.getString("factura"));
                List<String> factures = obtenirFacturesByAlbara(rs.getString("hisalb"), rs.getString("empresa"), artint, clicod);
                if (factures.size() > 0)
                    albaraBuilder.factura(String.join(",", factures));
                albaraBuilder.dataAlbaraFactura(rs.getDate("hisdia").toLocalDate())
                        .quantitat(rs.getLong("hisqua"))
                        .incoterm(rs.getString("incoterm"))
                        .mateixEnviamentDelClient(enviamentClient.equals(rs.getString("incoterm")))
                        .servida(rs.getString("servida"))
                        .codiTransportista(rs.getString("codi_transportista"));

                if (!rs.getString("hisalb").equals(ultimAlabaraRebut)) {
                    if (ultimAlabaraRebut.equals(""))
                        albaraBuilder.transit(0l);
                    else if (tallTransit)
                        albaraBuilder.transit(0l);
                    else
                        albaraBuilder.transit(transit);
                } else if (tallTransit) {
                    albaraBuilder.transit(0);
                } else {
                    albaraBuilder.transit(0);
                    tallTransit = true;
                }

                if (tallAcumulat)
                    albaraBuilder.acumulat(0L);
                else
                    albaraBuilder.acumulat(rs.getLong("hisqua"));
                if (!albaraAcumulat.equals("")) {
                    if (rs.getString("hisalb").equals(albaraAcumulat)) {
                        tallAcumulat = true;
                    }
                } else if (rs.getDate("hisdia").compareTo(Date.valueOf(finalDataAcumulat)) > 0) {
                    tallAcumulat = true;
                }
                resultat.add(albaraBuilder.build());
//					index++;
            }
//			}

            Collections.reverse(resultat);
            List<QueryAlbaransFacturesByArticleAndClientResponse> resultatOut = new ArrayList<QueryAlbaransFacturesByArticleAndClientResponse>();
            long quantitatAcumulada = estocAcumulatLong;
            long quantitatASumar = estocAcumulatLong;

            if (!albaraAcumulat.equals("")) {

                for (QueryAlbaransFacturesByArticleAndClientResponse item : resultat) {
                    QueryAlbaransFacturesByArticleAndClientResponseImpl.Builder builder = QueryAlbaransFacturesByArticleAndClientResponseImpl.builder()
                            .albara(item.albara())
//                    List<String> factures = obtenirFacturesByAlbara(item.albara(),empresa,artint,clicod);
//                    builder.factura(String.join(",",factures));
                            .factura(item.factura())
                            .quantitat(item.quantitat())
                            .dataAlbaraFactura(item.dataAlbaraFactura())
                            .transit(item.transit())
                            .incoterm(item.incoterm())
                            .mateixEnviamentDelClient(item.mateixEnviamentDelClient())
                            .servida(item.servida().get())
                            .codiTransportista(item.codiTransportista());
                    if (item.albara().compareTo(albaraAcumulat) == 0)
                        builder.acumulat(quantitatAcumulada);
                    else if (item.albara().compareTo(albaraAcumulat) > 0) {
                        quantitatASumar += item.quantitat();
                        builder.acumulat(quantitatASumar);
                    } else {
                        builder.acumulat(0);
                    }
                    resultatOut.add(builder.build());
                }
                Collections.reverse(resultatOut);

                return resultatOut;
            } else if (!finalDataAcumulat.equals("")) {
                for (QueryAlbaransFacturesByArticleAndClientResponse item : resultat) {
                    if (item.dataAlbaraFactura().compareTo(LocalDate.parse(finalDataAcumulat)) > 0) {
                        quantitatASumar += item.quantitat();
                        QueryAlbaransFacturesByArticleAndClientResponseImpl copia = QueryAlbaransFacturesByArticleAndClientResponseImpl.builder()
                                .albara(item.albara())
                                .factura(item.factura())
                                .dataAlbaraFactura(item.dataAlbaraFactura())
                                .quantitat(item.quantitat())
                                .transit(item.transit())
                                .acumulat(quantitatASumar)
                                .incoterm(item.incoterm())
                                .mateixEnviamentDelClient(item.mateixEnviamentDelClient())
                                .servida(item.servida().get())
                                .codiTransportista(item.codiTransportista())
                                .build();
                        resultatOut.add(copia);
                    } else
                        resultatOut.add(item);
                }
                Collections.reverse(resultatOut);

                return resultatOut;
            } else
                return resultatOut;


        };
        return new AdvantageDao().query(prep, rsAction);
    }

//	private long obteAcumulat(Long acumulat, List<Long> quantitats, int index) {
//		int indexQuantitats=1;
//		long quantitatAcumulada=0l;
//		for (Long quantitat : quantitats) {
//			if (indexQuantitats<index)
//				quantitatAcumulada+=quantitat;
//			else
//				return quantitatAcumulada;
//		}
//		return quantitatAcumulada;
//	}

    public List<QueryAlbaransFacturesByArticleAndClientResponse> queryFactura(String empresa, String artint, String clicod, String ultimaFacturaRebuda, String dataAcumulat, String estocAcumulat, String albaraAcumulat, String enviamentClient) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
//                    "select l.fccnum,l.fclq, l.fclalb,f.fccdat, f.fccenv " +
//                            "from faclin l,faccap f where f.empcod = l.empcod and f.fccnum = l.fccnum AND f.fcctip = f.fcctip " +
//                            "and l.empcod = ? and l.fclart = ? and l.codcli= ? and l.fcctip<'5' and l.tiplin='0001' and (fccdat>date()-600) order by l.fccnum desc"
                    "SELECT l.fccnum,l.fclq,l.fclalb,f.fccdat,f.fccenv,a.albtra codi_transportista,a.entregat entregat " +
                            "FROM faclin l " +
                            "INNER JOIN faccap f ON f.empcod = l.empcod AND f.fccnum = l.fccnum AND f.fcctip = l.fcctip " +
                            "LEFT JOIN albcap a ON a.albcod = l.fclalb AND a.clicod=l.codcli " +
                            "WHERE " +
                            "l.empcod = ? " +
                            "AND l.fclart = ? " +
                            "AND l.codcli = ? " +
                            "AND l.fcctip < '5' " +
                            "AND l.tiplin = '0001' " +
                            "AND (fccdat>date()-600) " +
                            "ORDER BY l.fccnum DESC "
            );

            statement.setString(1, empresa);
            statement.setString(2, artint);
            statement.setString(3, clicod);
            return statement;
        };

        ResultSetAction<List<QueryAlbaransFacturesByArticleAndClientResponse>> rsAction = rs -> {
            Long estocAcumulatLong = 0L;
            if (estocAcumulat != null)
                estocAcumulatLong = Long.parseLong(estocAcumulat);
            List<QueryAlbaransFacturesByArticleAndClientResponse> resultat = new ArrayList<QueryAlbaransFacturesByArticleAndClientResponse>();
            Long acumulat = 0L;
            Boolean tallTransit = false;
            boolean tallAcumulat = false;
            while (rs.next()) {
                acumulat = acumulat + rs.getLong("fclq");
                var factura = QueryAlbaransFacturesByArticleAndClientResponseImpl.builder()
                        .factura(rs.getString("fccnum"))
                        .albara(rs.getString("fclalb"))
                        .dataAlbaraFactura(rs.getDate("fccdat").toLocalDate())
                        .quantitat(rs.getLong("fclq"))
                        .incoterm(rs.getString("fccenv"))
                        .mateixEnviamentDelClient(enviamentClient.equals(rs.getString("fccenv")))
                        .servida(rs.getString("entregat"))
                        .codiTransportista(rs.getString("codi_transportista"));
                if (!rs.getString("fccnum").equals(ultimaFacturaRebuda)) {
                    if (ultimaFacturaRebuda.equals(""))
                        factura.transit(0l);
                    else if (tallTransit) {
                        factura.transit(0l);
                    } else
                        factura.transit(acumulat);
                } else if (tallTransit) {
                    factura.transit(0);
                } else {
                    factura.transit(0);
                    tallTransit = true;
                }

                if (tallAcumulat)
                    factura.acumulat(0L);
                else
                    factura.acumulat(0L);
//				if (!dataAcumulat.equals("") && rs.getDate("hisdia").compareTo(Date.valueOf(dataAcumulat))>0) {
//					tallAcumulat =true;
//				}
                resultat.add(factura.build());

            }

            Collections.reverse(resultat);

            List<QueryAlbaransFacturesByArticleAndClientResponse> resultatOut = new ArrayList<QueryAlbaransFacturesByArticleAndClientResponse>();
            long quantitatAcumulada = estocAcumulatLong;
            long quantitatASumar = estocAcumulatLong;
            if (!albaraAcumulat.equals("")) {
                for (QueryAlbaransFacturesByArticleAndClientResponse item : resultat) {
                    QueryAlbaransFacturesByArticleAndClientResponseImpl.Builder builder = QueryAlbaransFacturesByArticleAndClientResponseImpl.builder()
                            .albara(item.albara())
                            .factura(item.factura())
                            .dataAlbaraFactura(item.dataAlbaraFactura())
                            .quantitat(item.quantitat())
                            .transit(item.transit())
                            .incoterm(item.incoterm())
                            .mateixEnviamentDelClient(item.mateixEnviamentDelClient())
                            .servida(item.servida())
                            .codiTransportista(item.codiTransportista()); // Una factura sempre esta servida @pep
                    if (item.albara().compareTo(albaraAcumulat) == 0)
                        builder.acumulat(quantitatAcumulada);
                    else if (item.albara().compareTo(albaraAcumulat) > 0) {
                        quantitatASumar += item.quantitat();
                        builder.acumulat(quantitatASumar);
                    } else {
                        builder.acumulat(0);
                    }
                    resultatOut.add(builder.build());
                }
                Collections.reverse(resultatOut);

                return resultatOut;
            } else if (dataAcumulat != null && !dataAcumulat.equals("")) {
                for (QueryAlbaransFacturesByArticleAndClientResponse item : resultat) {
                    if (item.dataAlbaraFactura().compareTo(LocalDate.parse(dataAcumulat)) > 0) {
                        quantitatASumar += item.quantitat();
                        QueryAlbaransFacturesByArticleAndClientResponseImpl copia = QueryAlbaransFacturesByArticleAndClientResponseImpl.builder()
                                .albara(item.albara())
                                .factura(item.factura())
                                .dataAlbaraFactura(item.dataAlbaraFactura())
                                .quantitat(item.quantitat())
                                .transit(item.transit())
                                .acumulat(quantitatASumar)
                                .incoterm(item.incoterm())
                                .mateixEnviamentDelClient(item.mateixEnviamentDelClient())
                                .servida(item.servida())
                                .codiTransportista(item.codiTransportista())
                                .build();
                        resultatOut.add(copia);
                    } else
                        resultatOut.add(item);
                }
                Collections.reverse(resultatOut);

                return resultatOut;
            } else {
                Collections.reverse(resultat);
                return resultat;
            }
        };
        return new AdvantageDao().query(prep, rsAction);
    }

    public List<String> obtenirFacturesByAlbara(String albara, String empresa, String artint, String clicod) {
        PreparedStatementProvider prep = conn -> {
            var statement = conn.prepareStatement(
                    "SELECT DISTINCT(FCCNUM) FACTURA " +
                            "FROM faclin " +
                            "WHERE " +
                            "fclalb = ? " +
                            "AND fclart = ? " +
                            "AND codcli = ? " +
                            "AND empcod = ?"
            );

            statement.setString(1, albara);
            statement.setString(2, artint);
            statement.setString(3, clicod);
            statement.setString(4, empresa);
            return statement;
        };

        ResultSetAction<List<String>> rsAction = rs -> {
            List<String> resultat = new ArrayList<String>();
            while (rs.next()) {
                resultat.add(rs.getString("factura"));
            }
            return resultat;
        };
        return new AdvantageDao().query(prep, rsAction);
    }

//	public static boolean albaraComparator(final String albara1, final String albara2) {
//
//		String albara2_sensepunts = albara2.replaceAll("\\.", "");
//		String albara2_sensepunts_zero_prefix = "0" + albara2_sensepunts;
//
//		if (albara1.equals(albara2)
//				||
//				albara1.equals(albara2_sensepunts)
//				||
//				albara1.equals(albara2_sensepunts_zero_prefix))
//			return true;
//		else {
//			return false;
//
//		}
//	}

}

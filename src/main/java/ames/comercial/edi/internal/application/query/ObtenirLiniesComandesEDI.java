package ames.comercial.edi.internal.application.query;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.internal.*;
import ames.comercial.advantage.internal.response.QueryAlbaransFacturesByArticleAndClientResponse;
import ames.comercial.advantage.internal.response.QueryArticleClientEDIResponse;
import ames.comercial.comandes.ext.IObtenirLiniesComandaPendents;
import ames.comercial.comandes.internal.application.query.CalcularDataSortidaIntermitja;
import ames.comercial.comandes.internal.application.query.CalcularDiaSortidaArticleClient;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSON;
import ames.comercial.edi.internal.domain.DadesLiniaEDI;
import ames.comercial.edi.internal.infraestructure.query.QueryRepository;
import ames.comercial.edi.response.ComandaEDIResponse;
import ames.comercial.edi.response.ComandaEDIResponseImpl;
import ames.comercial.edi.response.LiniaEDIResponse;
import ames.comercial.edi.response.LiniaEDIResponseImpl;
import ames.comercial.inventari.ext.IObtenirStocks;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ObtenirLiniesComandesEDI {

    @Autowired
    ObtenirAlbaransFacturesByArticleAndClientAds obtenirAlbaransByArticleAndClientAds;

    @Autowired
    IObtenirLiniesComandaPendents iObtenirLiniesComandaPendents;

    @Autowired
    IObtenirClientAds obtenirClientAds;
    @Autowired
    IObtenirStocks obtenirStocksAds;
    @Autowired
    QueryRepository queryRepository;
    @Autowired
    ObtenirDadesAcumulatArticlesAds obtenirDadesAcumulatArticlesAds;
    @Autowired
    ObtenirArticleClientEDIAds obtenirArticleClientEDIAds;
    @Autowired
    CalcularDataSortidaIntermitja calcularDataSortidaIntermitja;
    @Autowired
    CalcularDiaSortidaArticleClient calcularDiaSortidaArticleClient;
    @Autowired
    ObtenirClientEDIAds obtenirClientEDIAds;

    static final Logger log = LogManager.getLogger(ObtenirLiniesComandesEDI.class.getName());

//    public ObtenirLiniesComandesEDI(ComandaEDIRepository comandaEDIRepository,
//                                    ObtenirComandesPerEDIAds obtenirComandesPerEDIAds,
//                                    ObtenirAlbaransFacturesByArticleAndClientAds obtenirAlbaransByArticleAndClientAds,
//                                    IObtenirClientAds obtenirClientAds,
//                                    ObtenirStocksAds obtenirStocksAds,
//                                    ObtenirDadesAcumulatArticlesAds obtenirDadesAcumulatArticlesAds) {
//        this.comandaEDIRepository = comandaEDIRepository;
//        this.obtenirComandesPerEDIAds = obtenirComandesPerEDIAds;
//        this.obtenirAlbaransByArticleAndClientAds = obtenirAlbaransByArticleAndClientAds;
//        this.obtenirClientAds = obtenirClientAds;
//        this.obtenirStocksAds = obtenirStocksAds;
//        this.obtenirDadesAcumulatArticlesAds = obtenirDadesAcumulatArticlesAds;
//    }

    public ComandaEDIResponse executar(Long codiComanda, String codiArticle, Boolean firstArticle) {

        Optional<DadesComandaEDINoJSON> dadesComandaEDI = getOrder(codiComanda);
        Optional<ObtenirClientAds.ClientAds> client = null;
        List<LiniaEDIResponse> liniesTotal = new ArrayList<LiniaEDIResponse>();
        List<LiniaEDIResponse> liniesEDI = new ArrayList<LiniaEDIResponse>();
        List<LiniaEDIResponse> liniesProcessades = new ArrayList<LiniaEDIResponse>();
        List<DadesLiniaEDI> dadesLiniaEDI = null;
        List<DadesLiniaEDI> dadesLiniaEDITotes = null;
//        LiniaEDIResponse liniaEDIResponse;
        StringBuffer logs = new StringBuffer();

        if (listLinesEDI(dadesComandaEDI.get().codi().get()).size() == 0)

            return ComandaEDIResponseImpl.builder().stockArticle(0L).comanda(dadesComandaEDI.get()).build();
        else {

            client = obtenirClientAds.get(dadesComandaEDI.get().codiClientAmes());
        }

//        String document = "";
//
//        if (dadesComandaEDI.get().document().equals("DELFOR")) {
//            document = "DELF";
//        }
//        if (dadesComandaEDI.get().document().equals("DELJIT")) {
//            document = "DELJ";
//        }

//        Optional<ObtenirClientEDIAds.ClientEDIAds> clientEDIAdsFlags = dadesComandaEDI.get().clientProfile();
        Optional<ObtenirClientEDIAds.ClientEDIAds> clientEDIAdsFlags = obtenirClientEDIAds.get(dadesComandaEDI.get().codiClientAmes(), dadesComandaEDI.get().document(), dadesComandaEDI.get().bustia());
        if (clientEDIAdsFlags.isEmpty()) {
            return null;
        }
        Boolean descomptarTransit = clientEDIAdsFlags.get().encoalb().get().equals("S") && !clientEDIAdsFlags.get().tipedi().equals("1");

        // Obtinc l'article a mostrar. Si no ha estat triat per l'usuari (desplegable), llavors agafo el primer de la "llista".
        Optional<KeyArticleAmes> article = null;
        String codiArticleFab = "";
        String magatzemSortida = "";
        String magatzemEntrada = "";
        Long stockArticle=0L;
        long quantitatEnTransit = 0;
        if (firstArticle != null) {
            article = queryRepository.getFirstArticleOfOrderLines(codiComanda); // Primer article de la llista
            dadesLiniaEDI = listLinesEDI(codiComanda, article.get().codiArticle(), clientEDIAdsFlags.get().diestall());
            dadesLiniaEDITotes = listLinesEDI(codiComanda);
        } else {
            if (codiArticle != null) {
                dadesLiniaEDI = listLinesEDI(codiComanda, codiArticle, clientEDIAdsFlags.get().diestall());
                KeyArticleAmesImpl.Builder articleBuilder = KeyArticleAmesImpl.builder();
                dadesLiniaEDITotes = listLinesEDI(codiComanda);
                article = Optional.of((articleBuilder.codiArticle(dadesLiniaEDI.get(0).codiArticle()).codiArticleAmes(dadesLiniaEDITotes.get(0).codiArticleAmes()).build()));
                int i=0;
                while (article.get().codiArticleAmes().startsWith("ERROR_") && dadesLiniaEDITotes.size()>i-1) {
                    article = Optional.of((articleBuilder.codiArticle(dadesLiniaEDI.get(i).codiArticle()).codiArticleAmes(dadesLiniaEDITotes.get(1).codiArticleAmes()).build()));
                }
            }
            else { // No es donara mai
                dadesLiniaEDI = listLinesEDI(codiComanda, clientEDIAdsFlags.get().diestall());
                KeyArticleAmesImpl.Builder articleBuilder = KeyArticleAmesImpl.builder();
                article = Optional.of(articleBuilder.codiArticle("*").codiArticleAmes("*").build());
            }
        }
        Optional<ObtenirDadesAcumulatArticlesAds.DadesAcumulatArticle> dadesAcumulats=null;
        KeyArticleClientImpl.Builder articleClientBuilder = KeyArticleClientImpl.builder();
        String referenciaNoTrobada ="";
        if (!dadesLiniaEDI.isEmpty()) {
            referenciaNoTrobada = dadesLiniaEDI.get(0).observacions();
        }
                if  (!article.isEmpty() && !article.get().codiArticleAmes().startsWith("ERROR")) {
                    List<QueryArticleClientEDIResponse> queryArticleClientResponse = obtenirArticleClientEDIAds.findByNADAndArtRef(dadesComandaEDI.get().nad(), article.get().codiArticleAmes());
                    magatzemSortida = queryArticleClientResponse.get(0).magatzemSortida();
                    magatzemEntrada = queryArticleClientResponse.get(0).magatzemEntrada();
                    stockArticle = queryArticleClientResponse.get(0).stock();
                    if (dadesLiniaEDI.size() > 0)
                        codiArticleFab = dadesLiniaEDI.get(0).codiArticleFab();
                    else {
                        codiArticleFab = queryArticleClientResponse.get(0).aclFab();
                    }
                    articleClientBuilder.artint(article.get().codiArticle()).clicod(dadesComandaEDI.get().codiClientAmes());

                    dadesAcumulats = obtenirDadesAcumulatArticlesAds.query(articleClientBuilder.build());

                    if (!dadesAcumulats.isEmpty())
                            log.info("dades: " + dadesAcumulats.get().albaraAcumulat());
                }
//        }

        ComandaEDIResponseImpl.Builder comandaEDIResponseBuilder = ComandaEDIResponseImpl.builder()
                .comanda(dadesComandaEDI.get())
                .codiClient(dadesComandaEDI.get().codiClientAmes())
                .nomClientAmes(dadesComandaEDI.get().nomClientAmes())
                .codiArticleAmes(article.get().codiArticleAmes())
                .codiArticleFab(codiArticleFab)
                .codiArticle(article.get().codiArticle())
                .missatgeNumero(dadesComandaEDI.get().missatgeNumero())
                .perfilDiesSortida(clientEDIAdsFlags.get().diessor())
                .perfilDiesResta(clientEDIAdsFlags.get().diesres())
                .stockArticle(stockArticle);
        int acumulatArticle=0;
        if (dadesLiniaEDI.size() > 0)
            acumulatArticle=dadesLiniaEDI.get(0).acumulatArticle();
        comandaEDIResponseBuilder.acumulatArticle(acumulatArticle);
        if (!dadesLiniaEDI.isEmpty() && !dadesLiniaEDI.get(0).codiArticle().startsWith("ERROR_REF")) {
            if (!dadesAcumulats.isEmpty()) {
                if (dadesAcumulats.get().dataAcumulat() != null && !dadesAcumulats.get().dataAcumulat().isEmpty())
                    comandaEDIResponseBuilder.dataAcumulat(Date.valueOf(dadesAcumulats.get().dataAcumulat()));
                if (dadesAcumulats.get().stockAcumulat() != null && !dadesAcumulats.get().stockAcumulat().isEmpty())
                    comandaEDIResponseBuilder.stockAcumulat(dadesAcumulats.get().stockAcumulat());
                if (dadesAcumulats.get().albaraAcumulat() != null && !dadesAcumulats.get().albaraAcumulat().isEmpty())
                    comandaEDIResponseBuilder.albaraAcumulat(dadesAcumulats.get().albaraAcumulat());
            }
        }
        if (!dadesComandaEDI.get().pathPDF().isEmpty())
            comandaEDIResponseBuilder.pathPDF(dadesComandaEDI.get().pathPDF().get());
        else
            comandaEDIResponseBuilder.pathPDF("");
        if (!dadesLiniaEDI.isEmpty())
            comandaEDIResponseBuilder.ultimAlbara(dadesLiniaEDI.get(0).ultimAlbara().get());
//        else {
//            comandaEDIResponseBuilder.ultimAlbara("-");
//            return comandaEDIResponseBuilder.build();
//        }
        // Càlcul de quantitat a descomptar al consultar els últims albarans. Aquesta seran totes les quantitats a partir de l'últim albara rebut (EDI) pel client fins la data d'avui
        List<Date> datesARestar = new ArrayList<Date>();
        if (!dadesLiniaEDI.isEmpty() && !dadesLiniaEDI.get(0).ultimAlbara().isEmpty() && !dadesLiniaEDI.get(0).ultimAlbara().get().equals("") && !article.get().codiArticle().startsWith("ERROR_REF")) {
            List<QueryAlbaransFacturesByArticleAndClientResponse> llistaAlbarans = llistatUltimsAlbarans(client, article.get().codiArticle(), dadesComandaEDI.get().codiClientAmes(), 10, dadesLiniaEDI.get(0).ultimAlbara().get(), dadesComandaEDI.get().clientProfile().get().empcod(), clientEDIAdsFlags.get().tipedi().equals("1"));
            boolean albaraFacturaTrobada = false;
            for (QueryAlbaransFacturesByArticleAndClientResponse facturaAlbara : llistaAlbarans) {
                if (facturaAlbara.albara().equals(dadesLiniaEDI.get(0).ultimAlbara().get()) || (!facturaAlbara.factura().isEmpty() && facturaAlbara.factura().get().equals(dadesLiniaEDI.get(0).ultimAlbara().get()))) {
                    albaraFacturaTrobada = true;
                    break;
                }
            }
            Optional<QueryAlbaransFacturesByArticleAndClientResponse> registreAmbQuantitatMaxima = llistaAlbarans.stream().max(Comparator.comparingLong(QueryAlbaransFacturesByArticleAndClientResponse::transit));
            if (!albaraFacturaTrobada)
                quantitatEnTransit = 0;
            else {
                if (registreAmbQuantitatMaxima.isEmpty())
                    quantitatEnTransit = 0;
                else
                    quantitatEnTransit = registreAmbQuantitatMaxima.get().transit();
            }

            String ultimAlbaraRebut = dadesLiniaEDI.get(0).ultimAlbara().get();

            Optional<QueryAlbaransFacturesByArticleAndClientResponse> ultimAlbaraRebutTrobat = llistaAlbarans.stream()
                    .filter(item -> ultimAlbaraRebut.equals(item.albara()))
                    .findFirst();
            if (ultimAlbaraRebutTrobat.isEmpty()) {
                ultimAlbaraRebutTrobat = llistaAlbarans.stream()
                        .filter(item -> item.factura()
                                .map(fact -> ultimAlbaraRebut.equals(fact))
                                .orElse(false))
                        .findFirst();
            }

            if (ultimAlbaraRebutTrobat.isEmpty())
                comandaEDIResponseBuilder.ultimAlbaraRebutTrobat(false);

            Boolean coincideixen=false;
            if (!ultimAlbaraRebut.isEmpty()) {
                // si acumulatArticle es 0 llavors no comprovem, considerem que coincideix
                coincideixen=(acumulatArticle==0 || (!ultimAlbaraRebutTrobat.isEmpty() && ultimAlbaraRebutTrobat.get().acumulat()==acumulatArticle));
            }

            comandaEDIResponseBuilder.coincideixenAcumulats(coincideixen);

        }

        int quantitatAcumulada = 0 - (int) quantitatEnTransit;

        if (descomptarTransit)
            logs.append("Quantitat a restar: " + quantitatEnTransit + ", treure tantes linies fins a arribar a aquesta quantitat pendent de servir\n");
        else
            logs.append("No resto " + quantitatEnTransit + " per que per aquest client no tenim el compte els albarans\n");

        log.trace("Quantitat a restar: " + quantitatEnTransit + ", treure tantes linies fins a arribar a aquesta quantitat pendent de servir");

//        Long articleDiesTransit = obtenirDiesTransitMagatzemByArticleAds.queryDies(article.get().codiArticle(), clientEDIAdsFlags.get().clicod());

        // Descomptem total o parcialment (quantitat) les línies d'acord a les comandes amb albarà posterior al rebut
        // Primer de tot ordeno per data Inicial per que si ha de descomptar començi per les mes "immediates" llavors si hi ha una linia amb atrassades es la que primer descomptarà
        dadesLiniaEDI.sort(Comparator.comparing(
                item -> item.dataInicial().orElse(null), // Desempaqueta el Optional
                Comparator.nullsLast(Comparator.naturalOrder()) // Maneja nulos
        ));


        for (DadesLiniaEDI liniaEDI : dadesLiniaEDI) {
            quantitatAcumulada = quantitatAcumulada + liniaEDI.quantitat();
            LiniaEDIResponseImpl.Builder liniaEDIResponseBuilder = LiniaEDIResponseImpl.builder()
                    .codi(liniaEDI.codi())
                    .codiComanda(liniaEDI.codi_comanda())
                    .tipus(clientEDIAdsFlags.get().ferori().get())
                    .codiArticle(liniaEDI.codiArticle())
                    .codiArticleAmes(liniaEDI.codiArticleAmes())
                    .codiComandaClient(liniaEDI.codiComandaClient())
                    .codiLiniaClient(liniaEDI.codiLiniaClient());
//                    .processable(true);
            liniaEDIResponseBuilder.quantitatProcessat(0);
            liniaEDIResponseBuilder.quantitatPendent(0);
            java.sql.Date dataClient = null;
            java.sql.Date dataAmes = null;

            if (!liniaEDI.dataInicial().isEmpty() && !liniaEDI.dataFinal().isEmpty()) {
                if (!clientEDIAdsFlags.get().dosdat().isEmpty() && clientEDIAdsFlags.get().dosdat().get().equals("S")) {
                    dataClient = liniaEDI.dataFinal().get();
                    dataAmes = liniaEDI.dataInicial().get();
                } else {
                    if (liniaEDI.dataInicial().get().compareTo(liniaEDI.dataFinal().get()) < 0) {
                        dataClient = liniaEDI.dataInicial().get();
                    } else {
//                        log.info("Les dates per aquest client estan mal posades, dataInicial es mes gran que la data final");
                        dataClient = liniaEDI.dataFinal().get();
                    }
                    // Ara agafem els dies de resta de la taula FLAGS
                    dataAmes = Date.valueOf(calcularDiaSortidaArticleClient.executar(dataClient.toLocalDate(),clientEDIAdsFlags.get().diesres(),clientEDIAdsFlags.get().diessor().get()));
                }
            }
            if (liniaEDI.dataInicial().isEmpty() && !liniaEDI.dataFinal().isEmpty()) {
                dataClient = liniaEDI.dataFinal().get();
            }
            if (!liniaEDI.dataInicial().isEmpty() && liniaEDI.dataFinal().isEmpty()) {
                dataClient = liniaEDI.dataInicial().get();
            }
            if (dataClient != null)
                liniaEDIResponseBuilder.dataClient(dataClient);
            if (dataAmes != null) {
                liniaEDIResponseBuilder.dataAMES(dataAmes);
            if (!magatzemEntrada.equals(magatzemSortida))
                liniaEDIResponseBuilder.dataMagatzem(java.sql.Date.valueOf(calcularDataSortidaIntermitja.executar(codiArticleFab+client.get().clicod(),dataAmes.toLocalDate())));
                else
                    liniaEDIResponseBuilder.dataMagatzem(Optional.empty());
            } else {
                LocalDate dataSortida = calcularDiaSortidaArticleClient.executar(dataClient.toLocalDate(),clientEDIAdsFlags.get().diesres(),clientEDIAdsFlags.get().diessor().get());
                liniaEDIResponseBuilder.dataAMES(java.sql.Date.valueOf(dataSortida));
                if (!magatzemEntrada.equals(magatzemSortida))
                  liniaEDIResponseBuilder.dataMagatzem(java.sql.Date.valueOf(calcularDataSortidaIntermitja.executar(codiArticleFab+client.get().clicod(),dataSortida)));
                else
                    liniaEDIResponseBuilder.dataMagatzem(Optional.empty());
            }

            long quantitatTotalMateixaData = obtenirQuantitatTotalMateixaData(dadesLiniaEDI, dataClient);
            if (descomptarTransit && (quantitatEnTransit > 0)) {
                liniaEDIResponseBuilder.quantitat((int) quantitatTotalMateixaData - (int) quantitatEnTransit);
//                quantitatAcumulada = quantitatAcumulada - (int) quantitatEnTransit;
            } else {
                liniaEDIResponseBuilder.quantitat((int) quantitatTotalMateixaData);
            }
            if (descomptarTransit)
                liniaEDIResponseBuilder.quantitatAcumulada(quantitatAcumulada - (int) quantitatEnTransit);
            else
                liniaEDIResponseBuilder.quantitatAcumulada(quantitatAcumulada);


            liniaEDIResponseBuilder.status(liniaEDI.status())
                    .observacions(liniaEDI.observacions())
//					.comentarisAMES("")
//					.alertesAMES("")
                    .ultimAlbara(liniaEDI.ultimAlbara())
                    .codiComandaClient(liniaEDI.codiComandaClient())
                    .codiLiniaClient(liniaEDI.codiLiniaClient())
                    .dataInicial(liniaEDI.dataInicial());
            if (!liniaEDI.dataFinal().isEmpty())
                liniaEDIResponseBuilder.dataFinal(liniaEDI.dataFinal());

//            DadesXAlbaraImpl.Builder dadesXAlbara =DadesXAlbaraImpl.builder();
//            dadesXAlbara.idCompr(dadesComandaEDI.get().nad())
//                    .idProve()

            if ((liniaEDI.quantitat() - (int) quantitatEnTransit) > 0 || !descomptarTransit)
                liniesEDI.add(liniaEDIResponseBuilder.build());
            else {
                datesARestar.add(dataClient);
                log.trace("Excloc aquesta linia (codi: " + liniaEDI.codi().get() + " data: " + dataClient + "), amb quantitat " + liniaEDI.quantitat() + " del total de " + quantitatEnTransit + ". Doncs s'entén que ja esta pendent de servir. es: " + liniaEDI.observacions());
                logs.append("Excloc aquesta linia (codi: " + liniaEDI.codi().get() + " data: " + dataClient + "), amb quantitat " + liniaEDI.quantitat() + " del total de " + quantitatEnTransit + ". Doncs s'entén que ja esta pendent de servir. es: " + liniaEDI.observacions() + "\n");
            }

            if (descomptarTransit) {
                if (quantitatEnTransit <= 0)
                    quantitatEnTransit = 0;
                else
                    quantitatEnTransit = quantitatEnTransit - liniaEDI.quantitat();
            }
        }

//		LiniaEDIResponse[] arrayLiniaEDIResponse = liniesTotal.stream().toArray(LiniaEDIResponse[]::new);

        /// Afegeixo les comandes pendents de servir i fem merge amb les liniesEDI

//        List<QueryComandesPerEDIResponse> liniesProcessadesActuals = obtenirComandesPerEDIAds.get(article.codiArticle(), dadesComandaEDI.get().codiClientAmes(),clientEDIAdsFlags.get().diestall());

//        if (liniesProcessadesActuals.size()>0)
//            System.out.println("Aquest client el podem migrar: " + dadesComandaEDI.get().codiClientAmes());

        List<IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse> liniesProcessadesActualsPostgres;
        KeyArticleClient keyArticleClient;
        if (dadesLiniaEDI.isEmpty() || dadesLiniaEDI.get(0).codiArticle().startsWith("ERROR_REF"))
            keyArticleClient = articleClientBuilder.artint(article.get().codiArticle()).clicod(client.get().clicod()).build();
        else {
            keyArticleClient = articleClientBuilder.build();
        }

            liniesProcessadesActualsPostgres = iObtenirLiniesComandaPendents.executar(keyArticleClient);

            LocalDate fechaLimite = RequestThread.dateLocal().plusDays(Long.parseLong(clientEDIAdsFlags.get().diestall()));

            liniesProcessadesActualsPostgres = liniesProcessadesActualsPostgres.stream()
                    .filter(response -> response.dataSolicitada().isBefore(fechaLimite))
                    .collect(Collectors.toList());

            log.trace("liniesProcessadesActualsPostgres " + liniesProcessadesActualsPostgres.size() + " pel client: " + keyArticleClient.clicod());

            for (IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse liniaProcessadaActual : liniesProcessadesActualsPostgres) {
                LiniaEDIResponseImpl.Builder liniaProcessadaActualBuilder = LiniaEDIResponseImpl.builder();

                if (liniaProcessadaActual.tipus() == TipusLiniaComanda.INVENT) {
                    liniaProcessadaActualBuilder.codi(0L)
                            .codiComanda(codiComanda)
                            .codiComandaClient(liniaProcessadaActual.comandaClient())
                            .codiLiniaProcessat(liniaProcessadaActual.clauLinia().numero())
                            .tipusProcessat(liniaProcessadaActual.tipus())
                            .tipus(liniaProcessadaActual.tipus())
                            .codiArticle(article.get().codiArticle())
                            .codiArticleAmes(article.get().codiArticleAmes())
                            .quantitat(liniaProcessadaActual.quantitatSolicitada().intValue())
                            .quantitatProcessat(liniaProcessadaActual.quantitatSolicitada().intValue())
                            .quantitatPendent(liniaProcessadaActual.quantitatPendent().intValue())
                            .quantitatAcumulada(0)
                            .dataClient(java.sql.Date.valueOf(liniaProcessadaActual.dataSolicitada()))
                            .dataAMES(java.sql.Date.valueOf(liniaProcessadaActual.dataSolicitada()));
                    if (!liniaProcessadaActual.dataConfirmadaFabrica().isEmpty())
                            liniaProcessadaActualBuilder.dataConfirmadaFabrica(java.sql.Date.valueOf(liniaProcessadaActual.dataConfirmadaFabrica().get()));
                    if (!magatzemEntrada.equals(magatzemSortida))
                        liniaProcessadaActualBuilder.dataMagatzem(java.sql.Date.valueOf(liniaProcessadaActual.dataSolicitada()));
                    liniaProcessadaActualBuilder.codiComandaClientProcessat(liniaProcessadaActual.comandaClient());
                    String comentaris = "";
//                    if (!liniaProcessadaActual.comentarisClient().isEmpty())
//                        comentaris+=liniaProcessadaActual.comentarisClient().get();
//                    if (!liniaProcessadaActual.comentarisInterns().isEmpty())
//                        comentaris+=liniaProcessadaActual.comentarisInterns().get();
                    liniaProcessadaActualBuilder.observacions(comentaris);
                    liniaProcessadaActualBuilder.status(DadesLiniaEDI.ENUM_STATUS_DRAFT);
                } else {
                    // Merge de comanda EDI amb el processat
                    Optional<LiniaEDIResponse> liniaEDITrobada = obteLiniaEDIAmbLaMateixaDataCodiComanda(liniesEDI, Date.valueOf(liniaProcessadaActual.dataSolicitada()), liniaProcessadaActual.comandaClient());
                    liniaProcessadaActualBuilder.codiComanda(codiComanda)
                            .tipusProcessat(liniaProcessadaActual.tipus())
                            .comentarisInterns(liniaProcessadaActual.comentarisInterns())
                            .comentarisClient(liniaProcessadaActual.comentarisClient())
                            .codiLiniaProcessat(liniaProcessadaActual.clauLinia().numero())
                            .codiComandaProcessat(liniaProcessadaActual.clauLinia().comanda())
                            .codiComandaClient(liniaProcessadaActual.comandaClient());
                            if (!liniaProcessadaActual.dataConfirmadaFabrica().isEmpty())
                                liniaProcessadaActualBuilder.dataConfirmadaFabrica(Date.valueOf(liniaProcessadaActual.dataConfirmadaFabrica().get()));
                            liniaProcessadaActualBuilder.codiArticle(article.get().codiArticle())
                            .codiArticleAmes(article.get().codiArticleAmes());
                    if (!liniaEDITrobada.isEmpty()) {
                        liniaProcessadaActualBuilder.codi(liniaEDITrobada.get().codi());
//                    liniaAProcessar=true;
                        Date dataClient;
                        if (!liniaEDITrobada.get().dataClient().isEmpty() && !liniaEDITrobada.get().dataClient().isEmpty())
                            dataClient = liniaEDITrobada.get().dataClient().get();
                        else
                            dataClient = Dates.getNowSQLDate();
                        liniaProcessadaActualBuilder.dataClient(dataClient);

                        Date dataAmes;
                        if (!liniaEDITrobada.get().dataAMES().isEmpty() && !liniaEDITrobada.get().dataAMES().isEmpty())
                            dataAmes=liniaEDITrobada.get().dataAMES().get();

                        else {
                            dataAmes=java.sql.Date.valueOf(calcularDiaSortidaArticleClient.executar(dataClient.toLocalDate(),clientEDIAdsFlags.get().diesres(),clientEDIAdsFlags.get().diessor().get()));
                        }
                        liniaProcessadaActualBuilder.dataAMES(dataAmes);
                        liniaProcessadaActualBuilder.dataInicial(liniaEDITrobada.get().dataInicial());
                        if (!liniaEDITrobada.get().dataFinal().isEmpty())
                            liniaProcessadaActualBuilder.dataFinal(liniaEDITrobada.get().dataFinal().get());

                        if (!magatzemEntrada.equals(magatzemSortida)) {
                            LocalDate dataMagatzem = calcularDataSortidaIntermitja.executar(codiArticleFab + client.get().clicod(), dataAmes.toLocalDate());
                            liniaProcessadaActualBuilder.dataMagatzem(Date.valueOf(dataMagatzem));
                        }
                        else
                            liniaProcessadaActualBuilder.dataMagatzem(Optional.empty());
//                        }
                        if (!liniaProcessadaActual.dataConfirmadaFabrica().isEmpty())
                            liniaProcessadaActualBuilder.dataConfirmadaFabrica(java.sql.Date.valueOf(liniaProcessadaActual.dataConfirmadaFabrica().get()));
                        liniaProcessadaActualBuilder.quantitatProcessat(liniaProcessadaActual.quantitatSolicitada().intValue())
                                .quantitatPendent(liniaProcessadaActual.quantitatPendent().intValue())
                                .codiComandaClientProcessat(liniaProcessadaActual.comandaClient());
                        String comentaris = "";
                        if (!liniaProcessadaActual.comentarisClient().isEmpty())
                            comentaris+=liniaProcessadaActual.comentarisClient().get();
                        if (!liniaProcessadaActual.comentarisInterns().isEmpty())
                            comentaris+=liniaProcessadaActual.comentarisInterns().get();
                        liniaProcessadaActualBuilder.observacions(comentaris);
                        liniaProcessadaActualBuilder.quantitat(liniaEDITrobada.get().quantitat());
                        liniaProcessadaActualBuilder.tipus(clientEDIAdsFlags.get().ferori().get())
                                .quantitatProcessat(liniaProcessadaActual.quantitatSolicitada().intValue())
                                .quantitatPendent(liniaProcessadaActual.quantitatPendent().intValue())
                                .quantitatAcumulada(liniaEDITrobada.get().quantitatAcumulada());
//                    if (liniaEDITrobada.get().quantitat()==liniaProcessadaActual.quantitat().intValue())
//                        liniaProcessadaActualBuilder.codiComandaClient(liniaProcessadaActual.comandaClient());
//                    else
                        liniaProcessadaActualBuilder.codiComandaClient(liniaEDITrobada.get().codiComandaClient());
                        liniaProcessadaActualBuilder.codiLiniaClient(liniaEDITrobada.get().codiLiniaClient());
                        //Si coincideixen la quantitat processada amb la del EDI no cal proccessar

                        liniaProcessadaActualBuilder.processable(true);
                        liniesEDI.removeIf(comanda ->
//						comanda.dataClient().isPresent() && comanda.dataClient().get().equals(Date.valueOf(liniaProcessadaActual.dataClient())));
                                comanda.dataClient().isPresent() && comanda.dataClient().get().toString().equals(liniaProcessadaActual.dataSolicitada().toString()) && comanda.codiComandaClient().isPresent() && comanda.codiComandaClient().get().equals((liniaProcessadaActual.comandaClient().split("/")[0])));
                        liniesTotal.remove(liniaEDITrobada);
                    } else {
//                    liniaAProcessar=true;
                        int quantitat = 0;
                        if (!existeixLiniaMateixaData(liniesEDI, liniaProcessadaActual.dataSolicitada())) {
                            // Poso la quantitat a 0 nomes si NO es un client tipus 4 (Ford i algun mes) i el tipus de comanda es un DELFOR
                            if
                            (
                                    (dadesComandaEDI.get().document().equals("DELFOR") && clientEDIAdsFlags.get().tipedi().equals("4") && liniaProcessadaActual.tipus().equals(TipusLiniaComanda.ORIENTATIU)) ||
//                                            (dadesComandaEDI.get().document().equals("DELFOR") && clientEDIAdsFlags.get().tipedi().equals("0") && liniaProcessadaActual.tipus().equals(TipusLiniaComanda.FERM)) ||
                                            (dadesComandaEDI.get().document().equals("DELFOR") && clientEDIAdsFlags.get().tipedi().equals("0") && liniaProcessadaActual.tipus().equals(TipusLiniaComanda.FERM)) ||
                                            ((dadesComandaEDI.get().document().equals("DELFOR") || dadesComandaEDI.get().document().equals("DELINS"))  && clientEDIAdsFlags.get().tipedi().equals("5") && liniaProcessadaActual.tipus().equals(TipusLiniaComanda.ORIENTATIU)) ||
                                            (dadesComandaEDI.get().document().equals("DELFOR") && clientEDIAdsFlags.get().tipedi().equals("1")) ||
                                            (dadesComandaEDI.get().document().equals("DELJIT") && clientEDIAdsFlags.get().tipedi().equals("3") && liniaProcessadaActual.tipus().equals(TipusLiniaComanda.FERM))
                            ) {
                                quantitat = 0;
                            } else
                                quantitat = liniaProcessadaActual.quantitatSolicitada().intValue();
                            liniaProcessadaActualBuilder.codiComandaClient(liniaProcessadaActual.comandaClient());
                        } else {
                            LiniaEDIResponse liniaEDIMateixaData = getLiniaFromDataClient(liniesEDI, Date.valueOf(liniaProcessadaActual.dataSolicitada()));
                            if (liniaEDIMateixaData.codiComandaClient().get().equals(liniaProcessadaActual.comandaClient())) {
                                quantitat = liniaEDIMateixaData.quantitat();
                                liniesEDI.remove(liniaEDIMateixaData);
                            } else {
                                if (dadesComandaEDI.get().document().equals("DELFOR") && liniaProcessadaActual.tipus().equals(TipusLiniaComanda.FERM) && clientEDIAdsFlags.get().tipedi().equals("4")) {
                                    liniaProcessadaActualBuilder.codiComandaClient(liniaProcessadaActual.comandaClient());
                                    quantitat = (int) (long) liniaProcessadaActual.quantitatSolicitada();
                                }
                                else {
                                    quantitat = 0;
                                    liniaProcessadaActualBuilder.codiComandaClient(liniaProcessadaActual.comandaClient());
                                }
                            }
                        }

                        liniaProcessadaActualBuilder.quantitat(quantitat)
                                .quantitatProcessat(liniaProcessadaActual.quantitatSolicitada().intValue())
                                .quantitatPendent(liniaProcessadaActual.quantitatPendent().intValue())
                                .tipus(liniaProcessadaActual.tipus())
                                .quantitatAcumulada(0)
                                .dataClient(java.sql.Date.valueOf(liniaProcessadaActual.dataSolicitada()))
                                .dataAMES(java.sql.Date.valueOf(liniaProcessadaActual.dataPrevistaSortida()));

                        if (!magatzemEntrada.equals(magatzemSortida))
                            liniaProcessadaActualBuilder.dataMagatzem(java.sql.Date.valueOf(calcularDataSortidaIntermitja.executar(codiArticleFab+client.get().clicod(),liniaProcessadaActual.dataSolicitada())));
                        else
                            liniaProcessadaActualBuilder.dataMagatzem(Optional.empty());
                        if (!liniaProcessadaActual.dataPrevistaSortidaInterna().isEmpty())
                            liniaProcessadaActualBuilder.dataMagatzem(java.sql.Date.valueOf(liniaProcessadaActual.dataPrevistaSortidaInterna().get()));
                        if (!liniaProcessadaActual.dataConfirmadaFabrica().isEmpty())
                            liniaProcessadaActualBuilder.dataConfirmadaFabrica(java.sql.Date.valueOf(liniaProcessadaActual.dataConfirmadaFabrica().get()));
                    }

                    liniaProcessadaActualBuilder.codiComandaClientProcessat(liniaProcessadaActual.comandaClient()).status(DadesLiniaEDI.ENUM_STATUS_DRAFT);
                    //comentarisClient(liniaProcessadaActual.comentarisClient()).comentarisInterns(liniaProcessadaActual.comentarisInterns()).observacions(liniaProcessadaActual.comentarisInterns()+" " + liniaProcessadaActual.comentarisClient())
//					.alertesAMES(liniaProcessadaActual.)
//					.comentarisAMES(liniaProcessadaActual.)

                }
                liniaProcessadaActualBuilder.codiLiniaProcessat(liniaProcessadaActual.clauLinia().numero())
                        .codiComandaProcessat(liniaProcessadaActual.clauLinia().comanda());
                // Els invents (I) els deixem igual pero posant com a quantitat nova la mateixa de l'actual per que no s'esborrin a l'hora de processar
//            if (liniaAProcessar)
                liniesTotal.add(liniaProcessadaActualBuilder.build());
            }

        for (LiniaEDIResponse liniaEDI : liniesEDI) {
            if (liniaEDI.quantitat() != 0)
                liniesTotal.add(liniaEDI);
        }

        // Post processat de linies de comanda per perfil//tipus de client (0-3)
        // 0: Només flags
        // 1: Plataforma
        // 2: (DELJIT/ORDER) Descomptar de previsions processades el ferm EDI
        // 3: (DELJIT/ORDER) Descomptar de processades el ferm EDI per Ford
        // 4: (DELFOR/DELINS) Descomptar de les previsions EDI el ferm processat
        // 5: De moment no fem res mes adicionalment, nomes NO tocar el ferm processat // Tipus de missatge
        // Descomptar=> restar quantitats partint de la linia amb data mes propera a la data d'avui, i segueixo per altres linies si encara queda quantitat per restar

        switch (clientEDIAdsFlags.get().tipedi()) {
//        switch ("2") {
            case "0":
                log.trace("Client de tipus 0=Només flags");
                break;
            case "1":
                log.trace("Client de tipus 1=Plataforma"); // DELFOR
                KeyArticleClientImpl.Builder keyArticleClientBuilder = KeyArticleClientImpl.builder();

//                if (dadesLiniaEDI.size()>0)
//                    keyArticleClientBuilder.artint(dadesLiniaEDI.get(0).codiArticle()).clicod(dadesComandaEDI.get().codiClientAmes()).build();
//                else
                    keyArticleClientBuilder.artint(article.get().codiArticle()).clicod(dadesComandaEDI.get().codiClientAmes()).build();

                    Empresa empresa = Empresa.getByClau(client.get().empresa());
                    Optional<Stock> stock = obtenirStocksAds.query(keyArticleClientBuilder.build(), empresa, magatzemSortida);
                    if (!stock.isEmpty()) {
                        LiniaEDIResponseImpl.Builder liniaStockEDIResponseBuilder = LiniaEDIResponseImpl.builder();
                        liniaStockEDIResponseBuilder
                                .codi(0)
                                .codiComanda(dadesComandaEDI.get().codi().get())
                                .codiComandaClient(dadesLiniaEDITotes.get(0).codiComandaClient().get())
                                .dataClient(Dates.getNowSQLDate())
                                .dataAMES(Dates.getNowSQLDate())
                                .dataMagatzem(Dates.getNowSQLDate())
                                .tipus(TipusLiniaComanda.FERM) //TODO revisar que sigui FERM el que ve dels stocks @pep
                                .observacions("QUANTITAT STOCK VE DE CLIENT \"PLATAFORMA\"")
                                .status(DadesComandaEDI.ENUM_STATUS_DRAFT)
                                .codiArticle(article.get().codiArticle())
                                .codiArticleAmes(article.get().codiArticleAmes());
                        int quantitatEnStock = (int) stock.get().stock();
                        liniaStockEDIResponseBuilder.quantitat(quantitatEnStock - (int) (quantitatEnTransit));
                        liniaStockEDIResponseBuilder.quantitatProcessat(0);
                        liniaStockEDIResponseBuilder.quantitatPendent(0);
                        liniesTotal.add(liniaStockEDIResponseBuilder.build());
                    }
                break;
            case "2":
                if (dadesComandaEDI.get().document().equals("DELJIT") || dadesComandaEDI.get().document().equals("ORDER")) {
                    log.trace("Restar de previsions processades el ferm EDI /// Tipus de missatge (DELJIT/ORDER) descompto aquestes de les previsions (Tipus=O) ja processades.");
                    int quantitatTotalEnFerm = 0;
                    List<LiniaEDIResponse> liniesProcessadesActualitzades2 = new ArrayList<LiniaEDIResponse>();
                    for (LiniaEDIResponse liniaEDI : liniesEDI) {
                        if (liniaEDI.tipus().equals(TipusLiniaComanda.FERM))
                            quantitatTotalEnFerm += liniaEDI.quantitat();
                    }

                    liniesTotal.sort(Comparator.comparing(
                            LiniaEDIResponse::dataClient,
                            Comparator.nullsLast(Comparator.comparing(opt -> opt == null ? null : opt.orElse(null), Comparator.nullsLast(Comparator.naturalOrder())))));
                    for (LiniaEDIResponse liniaProcessada : liniesTotal) {
                        if ((liniaProcessada.codi().isEmpty() || liniaProcessada.codi().equals(0L)) && quantitatTotalEnFerm>0) {
                            LiniaEDIResponse liniaProcessadaActualitzada = null;
                            if (liniaProcessada.tipus().equals(TipusLiniaComanda.ORIENTATIU)) {
                                int novaQuantitat = liniaProcessada.quantitatProcessat().get() - quantitatTotalEnFerm;
                                if (novaQuantitat >= 0) {
                                    liniaProcessadaActualitzada = LiniaEDIResponseImpl.builder().from(liniaProcessada).quantitat(novaQuantitat).build();
                                    //                        liniaProcessadaActualitzada.from(liniaProcessada).quantitat()

                                }
                                if (quantitatTotalEnFerm <= 0)
                                    liniesProcessadesActualitzades2.add(liniaProcessada);
                                else if (liniaProcessadaActualitzada != null)
                                    liniesProcessadesActualitzades2.add(liniaProcessadaActualitzada);
                                quantitatTotalEnFerm = quantitatTotalEnFerm - liniaProcessada.quantitatProcessat().get();
                            }
                            else
                                liniesProcessadesActualitzades2.add(liniaProcessada);
                        } else
                            liniesProcessadesActualitzades2.add(liniaProcessada);
                    }
//                    liniesTotal.removeIf(linia -> linia.codi().isEmpty() || linia.codi().get().equals(0));
//                    liniesTotal.removeIf(linia -> !linia.codi().isEmpty() && !linia.codi().get().equals(0));
                    liniesTotal.clear();
                    liniesTotal.addAll(liniesProcessadesActualitzades2);
                }
                break;
            case "3":
                if (dadesComandaEDI.get().document().equals("DELJIT") || dadesComandaEDI.get().document().equals("ORDER")) {
                    log.trace("Restar de processades el ferm EDI per Ford /// Tipus de missatge (DELJIT/ORDER) restar aquestes de les ja processades");
                    int quantitatTotalEnFerm = 0;
                    List<LiniaEDIResponse> liniesProcessadesActualitzades3 = new ArrayList<LiniaEDIResponse>();
                    for (LiniaEDIResponse liniaEDI : liniesEDI) {
                        if (liniaEDI.tipus().equals(TipusLiniaComanda.FERM) || liniaEDI.tipus().equals(TipusLiniaComanda.ORIENTATIU))
                            quantitatTotalEnFerm += liniaEDI.quantitat();
                    }

                    liniesTotal.sort(Comparator.comparing(
                            LiniaEDIResponse::dataClient,
                            Comparator.nullsLast(Comparator.comparing(opt -> opt == null ? null : opt.orElse(null), Comparator.nullsLast(Comparator.naturalOrder())))));
                    for (LiniaEDIResponse liniaProcessada : liniesTotal) {
                        if (liniaProcessada.codi().isEmpty() || liniaProcessada.codi().equals(0L)) {
                            LiniaEDIResponse liniaProcessadaActualitzada = null;
                            if (liniaProcessada.tipus().equals(TipusLiniaComanda.ORIENTATIU) || liniaProcessada.tipus().equals(TipusLiniaComanda.FERM)) { //NO EN ELS INVENTS
                                int novaQuantitat = liniaProcessada.quantitat() - quantitatTotalEnFerm;
                                if (novaQuantitat > 0) {
                                    String codiComandaClient = "";
                                    if (getLiniaFromDataClient(liniesEDI, liniaProcessada.dataClient().get()) != null)
                                        codiComandaClient = getLiniaFromDataClient(liniesEDI, liniaProcessada.dataClient().get()).codiComandaClient().get();
                                    else
                                        codiComandaClient = liniaProcessada.codiComandaClient().get();
                                    liniaProcessadaActualitzada = LiniaEDIResponseImpl.builder().from(liniaProcessada).quantitat(novaQuantitat).codiComandaClient(codiComandaClient).build();
                                    //                        liniaProcessadaActualitzada.from(liniaProcessada).quantitat()

                                } else
                                    liniaProcessadaActualitzada = LiniaEDIResponseImpl.builder().from(liniaProcessada).quantitat(0).build();
                                if (quantitatTotalEnFerm <= 0)
                                    liniesProcessadesActualitzades3.add(liniaProcessada);
                                else if (liniaProcessadaActualitzada != null)
                                    liniesProcessadesActualitzades3.add(liniaProcessadaActualitzada);
                                quantitatTotalEnFerm = quantitatTotalEnFerm - liniaProcessada.quantitatProcessat().get();
                            }
                        }
                    }
                    liniesTotal.removeIf(linia -> linia.codi().isEmpty() || linia.codi().get().equals(0));
//                    liniesTotal.removeIf(linia -> !linia.codi().isEmpty() && !linia.codi().get().equals(0));
                    liniesTotal.addAll(liniesProcessadesActualitzades3);
                }
                break;
            case "4":
                log.trace("Restar de les previsions EDI el ferm processat /// Tipus de missatge (DELFOR/DELINS) D'aquestes previsions que ens arriben descompto totes les comandes en ferm ja processades.");
//                List<LiniaEDIResponse> liniesEDIActualitzades4 = new ArrayList<LiniaEDIResponse>();
                int quantitatFermPendentDeRestar = 0;
                for (IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse liniaProcessada : liniesProcessadesActualsPostgres) {
                    if (liniaProcessada.tipus().equals(TipusLiniaComanda.FERM))
                        quantitatFermPendentDeRestar += liniaProcessada.quantitatSolicitada();
                }

                liniesEDI.sort(Comparator.comparing(
                        LiniaEDIResponse::dataClient,
                        Comparator.nullsLast(Comparator.comparing(opt -> opt == null ? null : opt.orElse(null), Comparator.nullsLast(Comparator.naturalOrder())))));

//                int novaQuantitatFermPendentDeRestar = quantitatFermPendentDeRestar;
                if (quantitatFermPendentDeRestar > 0) {
                    int novaQuantitatLinia;
                    for (LiniaEDIResponse liniaEDI : liniesEDI) {
                        if (quantitatFermPendentDeRestar > 0) {
                            liniesTotal.removeIf(linia -> linia.dataClient().get().equals(liniaEDI.dataClient().get()) && !linia.codi().isEmpty());
                            novaQuantitatLinia = liniaEDI.quantitat() - quantitatFermPendentDeRestar;
                            if (novaQuantitatLinia > 0) {
                                LiniaEDIResponse liniaEDIActualitzada = LiniaEDIResponseImpl.builder().from(liniaEDI).quantitat(novaQuantitatLinia).build();
                                //                            if (liniaEDIActualitzada.quantitat()!=0 || (!liniaEDIActualitzada.quantitatProcessat().isEmpty() && liniaEDIActualitzada.quantitatProcessat().get()!=0))
                                liniesTotal.add(liniaEDIActualitzada);
                            }
                            quantitatFermPendentDeRestar = quantitatFermPendentDeRestar - liniaEDI.quantitat();
                        }
                        else
                            break;
                    }
                }
//                for (LiniaEDIResponse liniaEDIResponse : liniesTotal) {
//                    if (existeixLiniaMateixaData(liniesEDIActualitzades4,liniaEDIResponse.dataClient().get().toLocalDate()))
//                        liniesTotal.remove(liniaEDIResponse);
//                }
//                liniesTotal.addAll(liniesEDIActualitzades4);
                break;
            case "5":

                break;
            default:
                log.warn("Tipus " + clientEDIAdsFlags.get().tipedi() + " no reconegut.");
        }

//        liniesTotal.addAll(liniesEDI);
        liniesTotal.addAll(liniesProcessades);



        liniesTotal.sort(Comparator.comparing(
                LiniaEDIResponse::dataClient,
                Comparator.nullsLast(Comparator.comparing(opt -> opt == null ? null : opt.orElse(null), Comparator.nullsLast(Comparator.naturalOrder()))
		)
//				.thenComparing(
//						LiniaEDIResponse::otroCampo,
//						Comparator.nullsLast(Comparator.reverseOrder())
//
        ));

        List<LiniaEDIResponse> liniesTotalAmbAcumulats = new ArrayList<LiniaEDIResponse>();

        Integer quantitatAcumuladaProcessat=0;
        Integer quantitatAcumuladaPendent = 0;
        Integer quantitatAcumuladaEDI=0;

        for (int i = 0; i < liniesTotal.size(); i++) {
            LiniaEDIResponseImpl.Builder liniaAmbAcumulatProcessat = LiniaEDIResponseImpl.builder().from(liniesTotal.get(i));
            quantitatAcumuladaProcessat+=liniesTotal.get(i).quantitatProcessat().get();
            quantitatAcumuladaEDI+=liniesTotal.get(i).quantitat();
            quantitatAcumuladaPendent+=liniesTotal.get(i).quantitatPendent().get();
            liniaAmbAcumulatProcessat.quantitatAcumuladaProcessat(quantitatAcumuladaProcessat);
            liniaAmbAcumulatProcessat.quantitatAcumuladaPendent(quantitatAcumuladaPendent);
            liniaAmbAcumulatProcessat.quantitatAcumulada(quantitatAcumuladaEDI);
            liniesTotalAmbAcumulats.add(liniaAmbAcumulatProcessat.build());
        }

        Collections.reverse(liniesTotalAmbAcumulats);

        comandaEDIResponseBuilder.linies(Optional.of(liniesTotalAmbAcumulats));

        comandaEDIResponseBuilder.log(logs.toString());

        //TODO Treure abans de pujar a producció
        return comandaEDIResponseBuilder.build();
    }

    private long obtenirQuantitatTotalMateixaData(List<DadesLiniaEDI> dadesLiniaEDI, Date dataClient) {
        long total = 0;
        for (DadesLiniaEDI liniaEDI : dadesLiniaEDI) {
            if ((!liniaEDI.dataFinal().isEmpty() && liniaEDI.dataFinal().get().equals(dataClient)) || (!liniaEDI.dataInicial().isEmpty() && liniaEDI.dataInicial().get().equals(dataClient)))
                total += liniaEDI.quantitat();
        }
        return total;
    }

    private LiniaEDIResponse getLiniaEDIEnFermARestarAmbMateixaData(List<LiniaEDIResponse> list, Optional<Date> date) {
        for (LiniaEDIResponse liniaEDIResponse : list) {
            if (liniaEDIResponse.dataClient().get().equals(date.get()) && liniaEDIResponse.tipus().equals(TipusLiniaComanda.FERM))
                return liniaEDIResponse;
        }
        return null;
    }

    private Optional<LiniaEDIResponse> obteLiniaEDIAmbLaMateixaDataCodiComanda(List<LiniaEDIResponse> liniesEDIResponse, Date dataEDI, String codiComandaClient) {
        for (LiniaEDIResponse comanda : liniesEDIResponse) {
            if (comanda.dataClient().isPresent() && comanda.dataClient().get().equals(dataEDI) && comanda.codiComandaClient().isPresent() && comanda.codiComandaClient().get().equals(codiComandaClient))
                return Optional.of(comanda);
        }
        return Optional.empty();
//		return liniesEDIResponse.stream()
//				.filter(comanda -> comanda.dataClient().isPresent() && comanda.dataClient().get().equals(dataEDI) && comanda.codiComandaClient().isPresent() && comanda.codiComandaClient().equals(codiComandaClient))
//				.findFirst();
    }

    public Optional<DadesComandaEDINoJSON> getOrder(Long codi) {
        return queryRepository.findNoJSON(codi);
    }

    public List<DadesLiniaEDI> listLinesEDI(Long codi_comanda, String codi_article, String diesPrevisio) {
        List<DadesLiniaEDI> list = queryRepository.listLinies(codi_comanda, diesPrevisio, codi_article);
        return list;
    }

    public List<DadesLiniaEDI> listLinesEDI(Long codi_comanda) {
        List<DadesLiniaEDI> list = queryRepository.listLinies(codi_comanda);
        return list;
    }

    public List<DadesLiniaEDI> listLinesEDI(Long codi_comanda, String diesPrevisio) {
        List<DadesLiniaEDI> list = queryRepository.listLinies(codi_comanda, diesPrevisio);
        return list;
    }

    public List<QueryAlbaransFacturesByArticleAndClientResponse> llistatUltimsAlbarans(Optional<ObtenirClientAds.ClientAds> client, String codiArticle, String codiClient, Integer top, String ultimAlbaraRebut, String codiEmpresa, boolean plataforma) {
        String dataAcumulat = null;
        String stockAcumulat;
        String albaraAcumulat;
        KeyArticleClientImpl.Builder articleClientBuilder = KeyArticleClientImpl.builder();
        articleClientBuilder.artint(codiArticle).clicod(codiClient);
        Optional<ObtenirDadesAcumulatArticlesAds.DadesAcumulatArticle> dadesAcumulats = obtenirDadesAcumulatArticlesAds.query(articleClientBuilder.build());

        dataAcumulat = dadesAcumulats.get().dataAcumulat();
        albaraAcumulat = dadesAcumulats.get().albaraAcumulat();
        stockAcumulat = dadesAcumulats.get().stockAcumulat();

        String enviamentClient = client.get().formaEnviament().codiAdvantage() + client.get().incoterm() + client.get().desti();

        if (!Character.isLetter(ultimAlbaraRebut.charAt(0))) {
            return obtenirAlbaransByArticleAndClientAds.queryAlbara(codiArticle, client.get().empresa(), codiClient, ultimAlbaraRebut, plataforma, dataAcumulat, stockAcumulat, albaraAcumulat,enviamentClient);
        } else {
//			return obtenirAlbaransByArticleAndClientAds.queryFactura(codiEmpresa, codiArticle, codiClient, top, ultimAlbaraRebut);
            return obtenirAlbaransByArticleAndClientAds.queryFactura(client.get().empresa(), codiArticle, codiClient, ultimAlbaraRebut, dataAcumulat,stockAcumulat,albaraAcumulat,enviamentClient);
        }
    }

    private boolean existeixLiniaMateixaData(List<LiniaEDIResponse> liniesProcessades, LocalDate data) {
        for (LiniaEDIResponse liniaProcessada : liniesProcessades) {
            if (liniaProcessada.dataClient().get().toString().equals(data.toString()))
                return true;
        }
        return false;
    }

//    private boolean existeixLinaMateixCodiComanda(List<LiniaEDIResponse> liniesProcessades, String codiComanda) {
//        for (LiniaEDIResponse liniaProcessada : liniesProcessades) {
//            if (liniaProcessada.codiComandaClient().get().toString().equals(codiComanda))
//                return true;
//        }
//        return false;
//    }
//

    private LiniaEDIResponse getLiniaFromDataClient(List<LiniaEDIResponse> llista, Date data) {
        for (LiniaEDIResponse liniaEDIResponse : llista) {
            if (liniaEDIResponse.dataClient().get().toString().equals(data.toString()))
                return liniaEDIResponse;
        }
        return null;
    }

//    private boolean existeixLiniaComandaProcessadaAmbData(List<IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse> liniesProcessadesActualsPostgres, Date data) {
//        for (IObtenirLiniesComandaPendents.ObtenirLiniesComandaPendentsResponse liniaProcessadesActualsPostgres : liniesProcessadesActualsPostgres) {
//            if (liniaProcessadesActualsPostgres.dataSolicitada().equals(data.toLocalDate()))
//                return true;
//        }
//        return false;
//    }

}
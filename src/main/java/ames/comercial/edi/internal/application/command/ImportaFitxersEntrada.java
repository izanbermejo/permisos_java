package ames.comercial.edi.internal.application.command;

import ames.comercial.advantage.internal.ObtenirArticleClientEDIAds;
import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.advantage.internal.response.QueryArticleClientEDIResponse;
import ames.comercial.edi.ComandaEDIConfig;
import ames.comercial.edi.beans.*;
import ames.comercial.edi.internal.application.service.FitxerEdiParser;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.domain.DadesComandaEDIImpl;
import ames.comercial.edi.internal.domain.DadesLiniaEDI;
import ames.comercial.edi.internal.domain.DadesLiniaEDIImpl;
import ames.comercial.edi.internal.infraestructure.comandaEDI.ComandaEDIRepository;
import ames.comercial.edi.internal.infraestructure.query.QueryRepository;
import ames.comercial.server.I18N;
import ames.comercial.server.exception.AppException;
import ames.comercial.shared.Dates;
import ames.comercial.shared.Numbers;
import ames.comercial.shared.StringTools;
import ames.comercial.shared.ValidationResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ImportaFitxersEntrada {

    @Autowired
    ComandaEDIRepository comandaEDIRepo;
    @Autowired
    QueryRepository queryRepository;
    @Autowired
    ObtenirArticleClientEDIAds obtenirArticleClientEDIAds;
    @Autowired
    ObtenirClientEDIAds obtenirClientEDIAds;
    @Autowired
    ComandaEDIConfig config;

    static final Logger log = LogManager.getLogger(ImportaFitxersEntrada.class.getName());

    String observacions = "";
    String observacionsLinia = "";
    String statusLinia = DadesLiniaEDI.ENUM_STATUS_DRAFT;

    public void run() {
        String pathEntrada = config.getDirIN();
        File directoriEntrada = new File(pathEntrada);

        File[] fitxersDirectori = directoriEntrada.listFiles();
        List<File> llistaFitxersEdi = fitxersDirectori != null ? List.of(fitxersDirectori) : List.of();
        for (var fitxerEdi : llistaFitxersEdi) {
            // Comprovació que no s'hagi processat aquest fitxer previament. En cas que sigui així es mostra al log
            // i es mou el fitxer al directori d'errors
            if (queryRepository.existComandaWithPathEDI(fitxerEdi.getName())) {
                log.warn("S'ha intentat processar un altre cop el fitxer EDI {} i s'ha mogut cap al directori d'error", fitxerEdi.getName());
                moveToErrorPath(fitxerEdi);
                continue;
            }

            // Tot el procés es fa dins d'un try-catch per a que si falla un fitxer s'intenti processar els altres i no
            // s'aturi el procés
            try {
                log.info("IMPORT_EDI Importació fitxer EDI {}", fitxerEdi.getName());
                List<ComandaMissatgeEDI> pedidos = new FitxerEdiParser(fitxerEdi).parse();
                for (ComandaMissatgeEDI pedido : pedidos) {
                    ValidationResult validationResult = validate(pedido);
                    //Dividim per tantes comandes com clients=consignataris hi ha al fitxer. Ford envia fitxers d'aquesta manera
                    Map<String, ComandaMissatgeEDI> pedidosPerClient = PedidoSplitter.dividirPedidoPorCodigoConsignatario(pedido);
                    for (String codiConsignatari : pedidosPerClient.keySet()) {
                        saveOrderToDatabase(pedidosPerClient.get(codiConsignatari), fitxerEdi, validationResult);
                    }
                }
                moveToBkpPath(fitxerEdi);
                log.info("IMPORT_EDI Importació fitxer EDI OK {}", fitxerEdi.getName());
            } catch (Exception e) {
                log.error("IMPORT_EDI Error en l'importació del fitxer EDI {}", fitxerEdi.getName(), e);
                moveToErrorPath(fitxerEdi);
            }
        }

    }

    public ValidationResult validate(ComandaMissatgeEDI pedido) {
        ValidationResult validationResult = new ValidationResult();
        // Comprovació que hagin línies
        if (pedido.getLineas() == null || pedido.getLineas().isEmpty()) {
            validationResult.invalidate("No hi han linies");
            return validationResult;
        }
        List<Linea> lineas = pedido.getLineas();
        for (Linea linea : lineas) {
            if (obtenirArticleClientEDIAds.findByArtRef(linea.getLA().getIdArticuloComprador()).isEmpty()) {
                validationResult.invalidate(I18N.getLiteral("edi.warning.referencianotrobada") + ": " + linea.getLA().getIdArticuloComprador());
                break;
            }
            if (linea.getLA().getIdArticuloComprador() == null) {
                validationResult.invalidate("Falta idArticuloComprador a un LA d'una linia");
                break;
            }
            List<Detalle> detalles = linea.getDetalles();
            if (detalles == null || detalles.isEmpty() || detalles.size() == 0)
                validationResult.invalidate("No hi ha detalls de la linia --> No podem saber la quantitat de peçes");
            else {
                for (Detalle detalle : detalles) {
                    if (detalle.getDA() == null)
                        validationResult.invalidate("Falta segment DA al detall --> No podem saber la quantitat de peçes");
                    else {
                        if (detalle.getDA().getCantidad() == null || detalle.getDA().getCantidad().isEmpty())
                            validationResult.invalidate("No podem saber la quantitat de peçes");
                    }
                }
            }
        }
        return validationResult;
    }

    @Transactional
    public void saveOrderToDatabase(ComandaMissatgeEDI pedido, File fitxerEdi, ValidationResult validationResult) {
//		LocalDate dataMaximaAntelacio = null;
        LocalDateTime fechalocal = null;
        String codiClient = "";
        String nomClient = "";
        String usuariLogistica = "";
        String status;
        Boolean ignoraComanda=false;
        if (validationResult.isValid())
            status = DadesComandaEDI.ENUM_STATUS_DRAFT;
        else
            status = DadesComandaEDI.ENUM_STATUS_WARNING;
        observacionsLinia = "";
        observacions = validationResult.getDescription();
        String nad = "";
        String nadpath = "";

//        String prefixPDFName ="XXXXX";
//        Path pathPDF = Paths.get(config.getDirpdfprefix()+"\\"+
//                Dates.getCurrentDateYear()+"\\"+
//                Dates.getCurrentDateMonth()+"\\"+
//                prefixPDFName+"_"+pathInEDI.getFileName().toString().split("_")[0]+".pdf");
        Path pathOutEDI = Paths.get(config.getDirBKP() + "/" + fitxerEdi.getName());

        // Obtenció de la data amb la data de modificació del fitxer
        fechalocal = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(fitxerEdi.lastModified()),
                ZoneId.systemDefault()
        );

        List<Linea> lineas = pedido.getLineas();

//                Map<String, KeyArticleClientFab> codisArticlesAmes = new HashMap<>();

        if (pedido.getLineas() != null && !pedido.getLineas().isEmpty()) {
            List<QueryArticleClientEDIResponse> artCliAdsList = null;
            // Si no podem obtenir el client a partir de la primera referencia, el busquem per les demes
            int iLinies = 0;
            String existingArticleId = "";
            while (iLinies < pedido.getLineas().size()) {
                artCliAdsList = obtenirArticleClientEDIAds.findByNADAndArtRef(pedido.getLineas().get(iLinies).getLC().getCodigoConsignatario(), pedido.getLineas().get(iLinies).getLA().getIdArticuloComprador());
                if (artCliAdsList.size() == 0) {
                    // "Referencia (" + pedido.getLineas().get(iLinies).getLA().getIdArticuloComprador() + ") no trobada\n";
                    if (!isComandaBuida(pedido.getLineas().get(iLinies).getDetalles()))
                        observacions += I18N.getLiteral("edi.comanda.observacions.referencia.notrobada", pedido.getLineas().get(iLinies).getLA().getIdArticuloComprador()) + "\n";
                    else {
                        observacions = I18N.getLiteral("edi.comanda.observacions.referencia.notrobada_buida", pedido.getLineas().get(iLinies).getLA().getIdArticuloComprador()) + "\n";
                        status = DadesComandaEDI.ENUM_STATUS_ERROR_REFERENCIA_ARTICLE_NOTFOUND_EMPTY;
                        usuariLogistica = "ERROR";
//                        ignoraComanda = true;
                    }
                    iLinies++;
                } else {
                    nad = pedido.getLineas().get(iLinies).getLC().getCodigoConsignatario();
                    nadpath = "LC/CodigoConsignatario=" + nad;
                    existingArticleId = pedido.getLineas().get(iLinies).getLA().getIdArticuloComprador();
                    break;
                }
            }
            if (artCliAdsList != null && artCliAdsList.size() == 1) {
                codiClient = artCliAdsList.get(0).clicod();
                nomClient = artCliAdsList.get(0).nomClient().get();
                usuariLogistica = artCliAdsList.get(0).usuariLogistica().get();
            } else if (artCliAdsList != null && artCliAdsList.size() > 1) {
                // Filtrem per nad que ve a la primera referencia trobada
                artCliAdsList = obtenirArticleClientEDIAds.findByNADAndArtRef(nad, existingArticleId);
                if (artCliAdsList.size() == 0) {
                    nad = pedido.getCI().getIdComprador();
                    nadpath = "CI/IdComprador=" + nad;
                    // intentem pel nad que ve a la capçelera de la comanda
                    artCliAdsList = obtenirArticleClientEDIAds.findByNADAndArtRef(nad, existingArticleId);
                    if (artCliAdsList.size() == 0) {
                        observacions += I18N.getLiteral("edi.comanda.observacions.nad.notrobat",nad) + "\n";
                        status = DadesComandaEDI.ENUM_STATUS_ERROR_NAD_NOTFOUND;
                    } else if (artCliAdsList.size() > 1) {
                        observacions += "edi.comanda.observacions.nad.multiple";
                        status = DadesComandaEDI.ENUM_STATUS_ERROR_NAD_DUPLICATED;
                    } else {
                        codiClient = artCliAdsList.get(0).clicod();
                        nomClient = artCliAdsList.get(0).nomClient().get();
                        usuariLogistica = artCliAdsList.get(0).usuariLogistica().get();
                    }
                } else if (artCliAdsList.size() == 1) {
                    codiClient = artCliAdsList.get(0).clicod();
                    nomClient = artCliAdsList.get(0).nomClient().get();
                    usuariLogistica = artCliAdsList.get(0).usuariLogistica().get();
                } else {
                    // Cas DAIMLER NAD y bustia duplicats
                    if (pedido.getLineas().size()>0 && !pedido.getLineas().get(0).getLA().getLugarEntrega().isEmpty() && (artCliAdsList.get(0).clicod().equals("407901")||artCliAdsList.get(0).clicod().equals("407903"))) {
                        if (pedido.getLineas().get(0).getLA().getLugarEntrega().equals("918")) {
                            Optional<QueryArticleClientEDIResponse> daimler = artCliAdsList.stream()
                                    .filter(art -> "407901".equals(art.clicod()))
                                    .findFirst();
                            codiClient = "407901";
                            nomClient = daimler.get().nomClient().get();
                            usuariLogistica = daimler.get().usuariLogistica().get();
                        } else if (pedido.getLineas().get(0).getLA().getLugarEntrega().equals("915")) {
                            Optional<QueryArticleClientEDIResponse> daimler = artCliAdsList.stream()
                                    .filter(art -> "407903".equals(art.clicod()))
                                    .findFirst();
                            codiClient = "407903";
                            nomClient = daimler.get().nomClient().get();
                            usuariLogistica = daimler.get().usuariLogistica().get();
                        }
                    } else {
                        // Peça amb evolució
                        codiClient = artCliAdsList.get(0).clicod();
                        nomClient = artCliAdsList.get(0).nomClient().get();
                        Optional<ObtenirClientEDIAds.ClientEDIAds> clientProfile = obtenirClientEDIAds.get(codiClient, pedido.getCA().getDocumento(), pedido.getCA().getBuzonOrigen());
                        // l'anem a buscar per la bustia
                        if (clientProfile.isEmpty()) {
                            codiClient = artCliAdsList.get(1).clicod();
                            nomClient = artCliAdsList.get(1).nomClient().get();
                        }
                        usuariLogistica = artCliAdsList.get(0).usuariLogistica().get();
                        status = DadesComandaEDI.ENUM_STATUS_WARNING;
                        String referencies="";
                        for (QueryArticleClientEDIResponse llista : artCliAdsList) {
                            referencies+=llista.aclFab()+",";
                        }
                        referencies = referencies.substring(0, referencies.length() - 1);
                        observacions+= I18N.getLiteral("edi.comanda.observacions.referencia.duplicada", referencies, artCliAdsList.get(0).clicod());
                    }
                }
            }
        }

        if (codiClient.equals("") && status!=DadesComandaEDI.ENUM_STATUS_ERROR_REFERENCIA_ARTICLE_NOTFOUND_EMPTY) {
            codiClient = DadesComandaEDI.ENUM_STATUS_ERROR_NAD_NOTFOUND;
            nomClient = DadesComandaEDI.ENUM_STATUS_ERROR_NAD_NOTFOUND;
            status = DadesComandaEDI.ENUM_STATUS_ERROR_NAD_NOTFOUND;
            usuariLogistica = "ERROR";
            observacions += I18N.getLiteral("edi.linies.observacions.nad",nad) + "\n";
        }



//		ObtenirClientEDIAds.ClientEDIAds clientProfile = obtenirClientEDIAds.get(codiClient,pedido.getCA().getDocumento()).orElseThrow(ComandesEDIException.ClientEDISenseProfile::new);
        Optional<ObtenirClientEDIAds.ClientEDIAds> clientProfile = obtenirClientEDIAds.get(codiClient, pedido.getCA().getDocumento(), pedido.getCA().getBuzonOrigen());

        DadesComandaEDIImpl.Builder dadesComandaEDI = DadesComandaEDIImpl.builder();
        dadesComandaEDI.json(pedido)
                .pathEDI(pathOutEDI.toString().replaceAll("\\\\", "/"))
                .numeroEnviament(pedido.getCA().getNumeroEnvio())
                .missatgeTipus(pedido.getCA().getTipo())
                .missatgeNumero(pedido.getCA().getNumeroDocumento())
                .data(fechalocal)
                .referencia(pedido.getCA().getReferencia())
                .usuariLogistica(usuariLogistica)
                .codiClientAmes(codiClient)
                .nomClientAmes(nomClient)
                .deleted(false)
                .insertedAt(LocalDateTime.now())
                .insertedBy("<SYSTEM>")
                .nad(nad)
                .nadPath(nadpath)
                .tipus(pedido.getCA().getDocumento())
                .bustia(pedido.getCA().getBuzonOrigen())
                .document(pedido.getCA().getDocumento());
        if (!clientProfile.isEmpty()) {
            dadesComandaEDI.clientProfile(clientProfile.get());
        } else {
            if (!codiClient.isEmpty()) {
                observacions += I18N.getLiteral("edi.comanda.observacions.flags.notrobats",codiClient);
                status = DadesComandaEDI.ENUM_STATUS_ERROR_CLIENT_SENSE_FLAGS;
            }
        }
//        if (validationResult.getDescription().startsWith("ERROR_REFERENCIA"))
//            status = DadesComandaEDI.ENUM_STATUS_ERROR_REFERENCIA_ARTICLE_NOTFOUND;
        dadesComandaEDI.status(status).observacions(observacions);

        if (!ignoraComanda) {
            Long codiComanda = comandaEDIRepo.save(dadesComandaEDI.build());

            List<DadesLiniaEDI> liniesBBDD = new ArrayList<DadesLiniaEDI>();

            String codiArticle = "";
            String codiArticleAmes = "";
            String codiArticleFab = "";
            String ultimAlbara = "";
            //        String dataUltimAlbara = "01/01/1900";

            // Obtencio de codi ultim albara
            // TODO MILLORA1 ordenar la llista d'albarans per la data

            for (Linea linea : pedido.getLineas()) {
                statusLinia = DadesLiniaEDI.ENUM_STATUS_DRAFT;
                observacionsLinia = "";
                List<AlbaranPrevio> liAlbarans = linea.getAlbaranesPrevios();
                if (liAlbarans != null && liAlbarans.size() > 0) {
                    ultimAlbara = liAlbarans.get(0).getAA().getReferenciaAlbaranEntrada();
                    if (Numbers.isNumeric(ultimAlbara)) {
                        if (ultimAlbara.length() < 7)
                            ultimAlbara = StringTools.replaceLast("0000000", ultimAlbara, ultimAlbara.length());
                        else {
                            ultimAlbara = ultimAlbara.substring(ultimAlbara.length() - 7);
                        }
                    }
                } else {
                    //                dataUltimAlbara = "01/01/1900";
                    ultimAlbara = "";
                }

                if (!codiClient.equals("")) {
                    List<QueryArticleClientEDIResponse> queryArticleClientResponse = obtenirArticleClientEDIAds.findByNADAndArtRef(nad, linea.getLA().getIdArticuloComprador());
                    if (queryArticleClientResponse.size() == 0) {
                        observacionsLinia += I18N.getLiteral("edi.linies.observacions.referencia.notrobada", linea.getLA().getIdArticuloComprador())+"\n";
                        statusLinia = DadesLiniaEDI.ENUM_STATUS_ERROR;
                        codiArticle = "ERROR_REF_" + linea.getLA().getIdArticuloComprador();
                        codiArticleAmes = "ERROR_REF_" + linea.getLA().getIdArticuloComprador();
                    } else if (queryArticleClientResponse.size() > 1) {
                        if (!checkCodiClient(queryArticleClientResponse, codiClient)) {
                            if ((codiClient.equals("407901")) || (codiClient.equals("407903"))) {
                                codiArticle = queryArticleClientResponse.get(0).artInt();
                                codiArticleAmes = queryArticleClientResponse.get(0).referencia();
                                codiArticleFab = queryArticleClientResponse.get(0).aclFab();
                            } else{
                                Optional<QueryArticleClientEDIResponse> articleClient = queryArticleClientResponse.stream()
                                        .filter(item -> item.clicod().equals(clientProfile.get().clicod()))
                                        .findFirst();
                                if (!articleClient.isEmpty()) {
                                    codiArticle = articleClient.get().artInt();
                                    codiArticleAmes = articleClient.get().referencia();
                                    codiArticleFab = articleClient.get().aclFab();
                                }
                                else {
                                    observacionsLinia += I18N.getLiteral("edi.linies.observacions.referencia.duplicada", linea.getLA().getIdArticuloComprador(), queryArticleClientResponse.get(0).nomClient().get());
                                    for (QueryArticleClientEDIResponse queryArticleClientEDIResponse : queryArticleClientResponse) {
                                        observacionsLinia += queryArticleClientEDIResponse.nomClient().get() + " // ";
                                    }
                                    statusLinia = DadesLiniaEDI.ENUM_STATUS_ERROR;
                                    codiArticle = "ERROR_REF_" + linea.getLA().getIdArticuloComprador();
                                    codiArticleAmes = "ERROR_REF_" + linea.getLA().getIdArticuloComprador();
                                }
                            }
                        } else {
                            observacionsLinia += I18N.getLiteral("edi.linies.observacions.referencia.duplicada",linea.getLA().getIdArticuloComprador(),queryArticleClientResponse.get(0).nomClient().get());
                            codiArticle = queryArticleClientResponse.get(0).artInt();
                            codiArticleAmes = queryArticleClientResponse.get(0).referencia();
                            codiArticleFab = queryArticleClientResponse.get(0).aclFab();
                        }
                    } else {
                        if (!codiClient.equals(queryArticleClientResponse.get(0).clicod())) {
                            statusLinia = DadesLiniaEDI.ENUM_STATUS_ERROR;
                            codiArticle = "ERROR_REF_" + linea.getLA().getIdArticuloComprador();
                            codiArticleAmes = "ERROR_REF_" + linea.getLA().getIdArticuloComprador();
                        } else {
                            codiArticle = queryArticleClientResponse.get(0).artInt();
                            codiArticleAmes = queryArticleClientResponse.get(0).referencia();
                            codiArticleFab = queryArticleClientResponse.get(0).aclFab();
                        }
                    }
                } else {
                    if (!observacionsLinia.startsWith(I18N.getLiteral("edi.linies.observacions.referencia.duplicada","",codiClient).substring(0,10))) {
                        I18N.getLiteral("edi.linies.observacions.referencia.duplicada","",codiClient);
                        statusLinia = DadesLiniaEDI.ENUM_STATUS_ERROR;
                    }
                }

                //				dataMaximaAntelacio = obteDataMaximaAntelacio(codiClient,clientProfile.get());

                List<Detalle> detalles = linea.getDetalles();
                //            int iDetalle=0;
                boolean liniaSenseData=false;
                for (Detalle detalle : detalles) {
                    DadesLiniaEDIImpl.Builder dadesLiniaEDI = null;
                    // Els tipus de DA no en fem cas per que per aixo tenim el flag FERORI
                    //                if (detalle.getDA().getTipo().equals("1") || detalle.getDA().getTipo().equals("4")) {

                    //					LocalDate localDateReferencia = getDataReferencia(detalle);
                    //					if (localDateReferencia != null && localDateReferencia.isBefore(dataMaximaAntelacio)) {
                    dadesLiniaEDI = DadesLiniaEDIImpl.builder()
                            .codi_comanda(codiComanda)
                            .codiArticle(codiArticle)
                            .codiArticleAmes(codiArticleAmes)
                            .codiArticleFab(codiArticleFab)
                            .quantitat(Integer.parseInt(detalle.getDA().getCantidad()));

                    if (detalle.getDA().getFechaInicial() != null && !detalle.getDA().getFechaInicial().isEmpty() && !detalle.getDA().getFechaInicial().equals("") && !detalle.getDA().getFechaInicial().equals("//"))
                        try {
                            dadesLiniaEDI.dataInicial(new java.sql.Date(Dates.formatter_simple_EDI.parse(detalle.getDA().getFechaInicial()).getTime()));
                        } catch (ParseException pe) {
                            observacionsLinia += "\nedi.linies.observacions.datainicial.novalida";
                            statusLinia = DadesLiniaEDI.ENUM_STATUS_WARNING;
                        }
                    else {
                        if (detalle.getDA().getFechaInicial().equals("//"))
                            liniaSenseData=true;
                        else {
                            liniaSenseData=true;
                            if (!observacionsLinia.contains(I18N.getLiteral("edi.linies.observacions.datainicial.novalida"))) {
                                observacionsLinia += "\n" + I18N.getLiteral("edi.linies.observacions.datainicial.novalida");
                            }
                            statusLinia = DadesLiniaEDI.ENUM_STATUS_WARNING;
                            if (detalle.getDA().getFechaFinal() != null && !detalle.getDA().getFechaFinal().isEmpty() && !detalle.getDA().getFechaFinal().equals("") && !detalle.getDA().getFechaFinal().equals("//")) {
                                try {
                                    dadesLiniaEDI.dataInicial(new java.sql.Date(Dates.formatter_simple_EDI.parse(detalle.getDA().getFechaFinal()).getTime()));
                                } catch (ParseException pe) {
                                    if (!observacionsLinia.contains(I18N.getLiteral("edi.linies.observacions.datafinal.novalida"))) {
                                        observacionsLinia += "\n" + I18N.getLiteral("edi.linies.observacions.datafinal.novalida");
                                        statusLinia = DadesLiniaEDI.ENUM_STATUS_WARNING;
                                    }
                                }
                            } else {
                                if (!observacionsLinia.contains(I18N.getLiteral("edi.linies.observacions.datafinal.novalida"))) {
                                    observacionsLinia += "\n" + I18N.getLiteral("edi.linies.observacions.datafinal.novalida");
                                }
                            }
                            statusLinia = DadesLiniaEDI.ENUM_STATUS_WARNING;
                        }
                    }
                    if (detalle.getDA().getFechaFinal() != null && !detalle.getDA().getFechaFinal().isEmpty() && !detalle.getDA().getFechaFinal().equals("") && !detalle.getDA().getFechaFinal().equals("//")) {
                        try {
                            dadesLiniaEDI.dataFinal(new java.sql.Date(Dates.formatter_simple_EDI.parse(detalle.getDA().getFechaFinal()).getTime()));
                        } catch (ParseException pe) {
                            if (!observacionsLinia.contains(I18N.getLiteral("edi.linies.observacions.datafinal.novalida"))) {
                                observacionsLinia += "\n" + I18N.getLiteral("edi.linies.observacions.datafinal.novalida");
                                statusLinia = DadesLiniaEDI.ENUM_STATUS_WARNING;
                            }
                        }
                    } else {
                        if (!observacionsLinia.contains(I18N.getLiteral("edi.linies.observacions.datafinal.novalida"))) {
                            observacionsLinia += "\n" + I18N.getLiteral("edi.linies.observacions.datafinal.novalida");
                            statusLinia = DadesLiniaEDI.ENUM_STATUS_WARNING;
                        }
                    }

                    if (!clientProfile.isEmpty() && !clientProfile.get().ferori().isEmpty()) {
                        //                    TipusLiniaComanda tipus;
                        //                    if (clientProfile.get().ferori().get().equals("F"))
                        //                        tipus = TipusLiniaComanda.FERM;
                        //                    else
                        //                        tipus = TipusLiniaComanda.ORIENTATIU;
                        dadesLiniaEDI.tipus(clientProfile.get().ferori().get().clauAdvantage());
                    } else {
                        if (Integer.parseInt(detalle.getDA().getTipo())<4)
                            dadesLiniaEDI.tipus("F");
                        else if (detalle.getDA().getTipo().equals("4"))
                            dadesLiniaEDI.tipus("O");
                        else {
                            dadesLiniaEDI.tipus("");
                            observacionsLinia += "\n" + "Tipus de linia" + detalle.getDA().getTipo() + " no suportada (1=FERM, 2=PREVISIONS)";
                            statusLinia = DadesLiniaEDI.ENUM_STATUS_WARNING;
                        }


                        if (observacionsLinia.contains(I18N.getLiteral("edi.linies.observacions.flags.notrobats",codiClient)))
                            observacionsLinia += I18N.getLiteral("edi.linies.observacions.flags.notrobats",codiClient);
                    }
                    dadesLiniaEDI.status(statusLinia);
                    dadesLiniaEDI.observacions(observacionsLinia);
                    dadesLiniaEDI.insertedAt(LocalDateTime.now());
                    dadesLiniaEDI.insertedBy("<SYSTEM>");
                    dadesLiniaEDI.ultimAlbara(ultimAlbara);
                    String codiComandaClient = "";
                    if (pedido.getCA().getDocumento().equals("DELJIT") || pedido.getCA().getDocumento().substring(0,4).equals("ORDE") ) {
                        if (!linea.getDetalles().get(0).getDA().getNumeroRAN().equals(""))
                            //                        dadesLiniaEDI.codiComandaClient(linea.getDetalles().get(0).getDA().getNumeroRAN());
                            codiComandaClient = linea.getDetalles().get(0).getDA().getNumeroRAN();
                        else {
                            //                        dadesLiniaEDI.codiComandaClient(linea.getLG().getNumeroContrato());
                            codiComandaClient = linea.getLG().getNumeroContrato();
                        }
                    } else {
                        if (linea.getLG() != null)
                            codiComandaClient = linea.getLG().getNumeroContrato();
                        else {
                            dadesLiniaEDI.status(DadesComandaEDI.ENUM_STATUS_ERROR_CODI_COMANDA_NO_INFORMAT);
                            dadesLiniaEDI.observacions(I18N.getLiteral("edi.linies.observacions.codicomandaclient.notrobat"));
                        }

                    }
                    //                    dadesLiniaEDI.codiComandaClient(linea.getLG().getNumeroContrato());

                    if (!codiComandaClient.equals(""))
                        dadesLiniaEDI.codiComandaClient(codiComandaClient);
                    else {
                        dadesLiniaEDI.status(DadesComandaEDI.ENUM_STATUS_ERROR_CODI_COMANDA_NO_INFORMAT);
                        dadesLiniaEDI.observacions(I18N.getLiteral("edi.linies.observacions.codicomandaclient.notrobat"));
                    }
                    if (linea.getLineasPedidoPrevio().size() == 1 && !linea.getLineasPedidoPrevio().get(0).getLQ().getCantidadAcumuladaRecibida().trim().equals(""))
                        dadesLiniaEDI.acumulatArticle(Integer.parseInt(linea.getLineasPedidoPrevio().get(0).getLQ().getCantidadAcumuladaRecibida()));
                    else
                        dadesLiniaEDI.acumulatArticle(0);

                    if (linea.getLG() != null)
                        dadesLiniaEDI.codiLiniaClient(linea.getLG().getNumeroLineaContrato());

                    if (!liniaSenseData) // @pep al condi antic ignorem aquelles linies que venen amb fechaInicial=//
                        liniesBBDD.add(dadesLiniaEDI.build());
                }
                if (linea.getLineasPedidoPrevio().size() == 1 && !linea.getLineasPedidoPrevio().get(0).getLQ().getCantidadAtraso().trim().equals("") && Integer.parseInt(linea.getLineasPedidoPrevio().get(0).getLQ().getCantidadAtraso()) > 0) {
                    DadesLiniaEDIImpl.Builder dadesLiniaEDIAtrassat = DadesLiniaEDIImpl.builder();
                    dadesLiniaEDIAtrassat.codi_comanda(codiComanda)
                            .codiArticle(codiArticle)
                            .codiArticleAmes(codiArticleAmes)
                            .codiArticleFab(codiArticleFab)
                            .quantitat(Integer.parseInt(linea.getLineasPedidoPrevio().get(0).getLQ().getCantidadAtraso()))
                            .dataInicial(Dates.getNowSQLDate())
                            .tipus("F")
                            .ultimAlbara(ultimAlbara)
                            .status(DadesLiniaEDI.ENUM_STATUS_WARNING)
                            .observacions(I18N.getLiteral("edi.linies.observacions.atrassades"))
                            .insertedAt(LocalDateTime.now())
                            .insertedBy("<SYSTEM>")
                            .codiComandaClient(linea.getLG().getNumeroContrato())
                            .codiLiniaClient(linea.getLG().getNumeroLineaContrato());


                    if (linea.getLineasPedidoPrevio().size() == 1 && !linea.getLineasPedidoPrevio().get(0).getLQ().getCantidadAcumuladaRecibida().trim().equals(""))
                        dadesLiniaEDIAtrassat.acumulatArticle(Integer.parseInt(linea.getLineasPedidoPrevio().get(0).getLQ().getCantidadAcumuladaRecibida()));
                    else
                        dadesLiniaEDIAtrassat.acumulatArticle(0);

                    liniesBBDD.add(dadesLiniaEDIAtrassat.build());

                }
            }

//            if (codiClient!=null)//) && codiArticle!=null)
//                BeanUtils.getBean(MigracioComandes.class).migracio(codiClient,null);

            // DONA ERROR, mirar de fer-ho un cop insertat a BBDD comprovar quan hi hagin mes per una mateixa data i llavors sumar quantitats
            if (liniesBBDD.stream().allMatch(d -> d.dataInicial().isPresent()))
                liniesBBDD = agruparPerDataInicial(liniesBBDD);
            else if (liniesBBDD.stream().allMatch(d -> d.dataFinal().isPresent()))
                liniesBBDD = agruparPerDataFinal(liniesBBDD);



            comandaEDIRepo.saveLinies(liniesBBDD);
        }
//		}
    }

//	private LocalDate obteDataMaximaAntelacio(String codiClient,ObtenirClientEDIAds.ClientEDIAds clientProfile) {
//		if (codiClient==null || )
//			return Dates.getLocalDatePlusDays(180);
//		else
//			return Dates.getLocalDatePlusDays(180); // TODO en alguns casos son 8 mesos dependrà del client
//	}

    private static List<DadesLiniaEDI> agruparPerDataInicial(List<DadesLiniaEDI> dadesLinies) {
        return new ArrayList<>(dadesLinies.stream()
                .collect(Collectors.groupingBy(d -> new ClaveAgrupacion(
                        d.dataInicial()
                                .map(java.sql.Date::toLocalDate)  // Evita llamar .get()
                                .orElseGet(() -> d.dataFinal().map(java.sql.Date::toLocalDate).orElse(null)), // Usa dataFinal si dataInicial no está
                        d.codi_comanda(),
                        d.codiArticle(),
                        d.codiArticleAmes(),
                        d.codiArticleFab(),
                        d.tipus(),
                        d.status()
                )))
                .values()
                .stream()
                .map(grup -> {
                    int sumaQuantitat = grup.stream().mapToInt(DadesLiniaEDI::quantitat).sum();
                    DadesLiniaEDI dades = grup.get(0);

                    return DadesLiniaEDIImpl.builder()
                            .dataInicial(dades.dataInicial()) // Mantiene el Optional original
                            .dataFinal(dades.dataFinal())
                            .quantitat(sumaQuantitat)
                            .codi_comanda(dades.codi_comanda())
                            .codiArticle(dades.codiArticle())
                            .codiArticleAmes(dades.codiArticleAmes())
                            .codiArticleFab(dades.codiArticleFab())
                            .tipus(dades.tipus())
                            .observacions(dades.observacions())
                            .status(dades.status())
                            .insertedAt(dades.insertedAt())
                            .insertedBy(dades.insertedBy())
                            .acumulatArticle(dades.acumulatArticle())
                            .ultimAlbara(dades.ultimAlbara())
                            .codiLiniaClient(dades.codiLiniaClient())
                            .codiComandaClient(dades.codiComandaClient())
                            .build();
                })
                .collect(Collectors.toList()));
    }

    private static List<DadesLiniaEDI> agruparPerDataFinal(List<DadesLiniaEDI> dadesLinies) {
        return new ArrayList<>(dadesLinies.stream()
                .collect(Collectors.groupingBy(d -> new ClaveAgrupacion(
                        d.dataInicial()
                                .map(date -> date.toLocalDate())  // Conversión correcta
                                .orElseGet(() -> d.dataFinal().map(date -> date.toLocalDate()).orElse(null)), // Usa dataFinal si dataInicial no está
                        d.codi_comanda(),
                        d.codiArticle(),
                        d.codiArticleAmes(),
                        d.codiArticleFab(),
                        d.tipus(),
                        d.status()
                )))
                .values()
                .stream()
                .map(grup -> {
                    int sumaQuantitat = grup.stream().mapToInt(DadesLiniaEDI::quantitat).sum();
                    DadesLiniaEDI dades = grup.get(0);

                    return DadesLiniaEDIImpl.builder()
                            .dataInicial(dades.dataInicial()) // Mantiene el Optional original
                            .dataFinal(dades.dataFinal())
                            .quantitat(sumaQuantitat)
                            .codi_comanda(dades.codi_comanda())
                            .codiArticle(dades.codiArticle())
                            .codiArticleAmes(dades.codiArticleAmes())
                            .codiArticleFab(dades.codiArticleFab())
                            .tipus(dades.tipus())
                            .observacions(dades.observacions())
                            .status(dades.status())
                            .insertedAt(dades.insertedAt())
                            .insertedBy(dades.insertedBy())
                            .acumulatArticle(dades.acumulatArticle())
                            .ultimAlbara(dades.ultimAlbara())
                            .codiLiniaClient(dades.codiLiniaClient())
                            .codiComandaClient(dades.codiComandaClient())
                            .build();
                })
                .collect(Collectors.toList()));
    }


    private record ClaveAgrupacion(LocalDate dataInicial, Long codiComanda, String codiArticle, String codiArticleAmes,
                                   String codiArticleFab, String tipus, String status) {
    }

    private void moveToBkpPath(File fitxerEdi) {
        Path desti = Paths.get(config.getDirBKP()).resolve(fitxerEdi.getName());
        try {
            Files.move(fitxerEdi.toPath(), desti, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            String missatge = String.format( "Error al moure el fitxer processat %s al directori %s", fitxerEdi.getName(), desti);
            log.error(missatge);
            throw new AppException(missatge);
        }
    }

    private void moveToErrorPath(File fitxerEdi) {
        Path desti = Paths.get(config.getDirERROR()).resolve(fitxerEdi.getName());
        try {
            Files.move(fitxerEdi.toPath(), desti, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            String missatge = String.format( "Error al moure el fitxer amb errors %s al directori %s", fitxerEdi.getName(), desti);
            log.error(missatge);
            throw new AppException(missatge);
        }
    }

//	private boolean comandaSenseLiniesAProcessar(Pedido pedido) {
//		boolean comandaSenseLiniesAProcessar = true;
//		List<Linea> lineas = pedido.getLineas();
//		for (Linea linea : lineas) {
//			List<Detalle> detalles = linea.getDetalles();
//			for (Detalle detalle : detalles) {
//				if (!detallAmbDataForaDePrevisio(detalle,linea.getLC().getCodigoConsignatario()))
//					return false;
//			}
//		}
//		return comandaSenseLiniesAProcessar;
//	}

//	private boolean detallAmbDataForaDePrevisio(Detalle detalle, String codiClient) {
//		LocalDate localDateReferencia = getDataReferencia(detalle);
//		return (localDateReferencia == null || localDateReferencia.isAfter(obteDataMaximaAntelacio(codiClient)));
//	}

//    private LocalDate getDataReferencia(Detalle detalle) {
//        Date dateDataInicial = null;
//        Date dateDataFinal = null;
//        LocalDate localDateReferencia = null;
//        if (detalle.getDA().getFechaInicial() != null && !detalle.getDA().getFechaInicial().isEmpty() && !detalle.getDA().getFechaInicial().equals("") && !detalle.getDA().getFechaInicial().equals("//")) {
//            try {
//                dateDataInicial = new java.sql.Date(Dates.formatter_simple_EDI.parse(detalle.getDA().getFechaInicial()).getTime());
//            } catch (ParseException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        if (detalle.getDA().getFechaFinal() != null && !detalle.getDA().getFechaFinal().isEmpty() && !detalle.getDA().getFechaFinal().equals("") && !detalle.getDA().getFechaFinal().equals("//")) {
//            try {
//                dateDataFinal = new java.sql.Date(Dates.formatter_simple_EDI.parse(detalle.getDA().getFechaFinal()).getTime());
//            } catch (ParseException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        if (dateDataInicial == null && dateDataFinal == null) {
//            observacionsLinia += "\nedi.linies.observacions.datesnoinformades";
//            statusLinia = DadesLiniaEDI.ENUM_STATUS_WARNING;
//        } else {
//            if (dateDataFinal == null) {
//                if (Dates.isValidEDIDate(detalle.getDA().getFechaInicial()))
//                    localDateReferencia = LocalDate.parse(detalle.getDA().getFechaInicial(), Dates.formatter_datetime);
//                else
//            } else {
//                if (Dates.isValidEDIDate(detalle.getDA().getFechaFinal()))
//                    localDateReferencia = LocalDate.parse(detalle.getDA().getFechaFinal(), Dates.formatter_datetime);
//                else
//
//            }
//        }
//        return localDateReferencia;
//    }

    private static boolean isComandaBuida(List<Detalle> detalles) {
        int quantitat=0;
        for (Detalle detalle : detalles) {
            quantitat+=Integer.parseInt(detalle.getDA().getCantidad());
        }
        return quantitat==0;
    }

    private static String cropFileExtension(String nombreArchivo) {
        return nombreArchivo.replaceFirst("[.][^.]+$", "");
    }

    private static String changeFileExtension(String nombreArchivo, String nuevaExtension) {
        String nombreArchivoSinExtension = cropFileExtension(nombreArchivo); // Elimina la extensión actual
        String nuevoNombreArchivo = nombreArchivoSinExtension + "." + nuevaExtension;

        return nuevoNombreArchivo;
    }

    private Boolean checkCodiClient(List<QueryArticleClientEDIResponse> queryArticleClientResponse, String clicod) {
        for (QueryArticleClientEDIResponse queryArticleClientEDIResponse : queryArticleClientResponse) {
            String currentClicod = queryArticleClientEDIResponse.clicod();
            if (!currentClicod.equals(clicod))
                return false;
        }
        return true;
    }

//	@Transactional
//	public void executar (Comanda c) {
//		var servida = c.servida();
//		var quantitatPendent = liniaComandaRepo.quantitatPendent(c.codi());
//		if (servida && quantitatPendent >= 0) {
//			// Si està marcada com a servida però hi han quantitats pendents s'actualitza a pendent
//			c.marcarPendent();
//			saveAndPublishEvent(c);
//		} else if (!servida && quantitatPendent == 0) {
//			// Si està marcada com a no servida però no hi han quantitats pendents s'actualitza a servida
//			c.marcarServida();
//			saveAndPublishEvent(c);
//		}
//	}

//	private void saveAndPublishEvent(Comanda c) {
//		comandaRepo.save(c);
//		applicationEventPublisher.publishEvent(new EstatComandaActualitzatEvent(this, c));
//
//	}

}

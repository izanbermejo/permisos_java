package ames.comercial.edi.internal.application.command;

import ames.comercial.comandes.ext.IProcessarLiniesComanda;
import ames.comercial.comandes.ext.ProcessarLiniesComandaReqImpl;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComandaImpl;
import ames.comercial.comandes.internal.domain.linia.LiniaComandaEDI;
import ames.comercial.edi.beans.ComandaMissatgeEDI;
import ames.comercial.edi.beans.Linea;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.infraestructure.comandaEDI.ComandaEDIRepository;
import ames.comercial.edi.internal.infraestructure.query.QueryRepository;
import ames.comercial.edi.response.ComandaEDIResponse;
import ames.comercial.edi.response.LiniaEDIResponse;
import ames.comercial.server.Json;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.KeyArticleClientImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProcesaLiniaArticle {

    @Autowired
    ComandaEDIRepository comandaEDIRepository;
    @Autowired
    IProcessarLiniesComanda processarLiniesComanda;

    QueryRepository queryRepository;
    private @Autowired ObjectMapper jsonMapper;
    private Json json;
    static final Logger log = LogManager.getLogger(ProcesaLiniaArticle.class);

    @PostConstruct
    public void init() {
        json = new Json(jsonMapper);
    }

    public ProcesaLiniaArticle(ComandaEDIRepository comandaEDIRepository, QueryRepository queryRepository) {
        this.comandaEDIRepository = comandaEDIRepository;
        this.queryRepository = queryRepository;
    }

    public void executar(Long codiComanda, String codiArticle, ComandaEDIResponse comanda) {
        // Envio a processar les linies de comanda processables (EDI o fusionades amb EDI) i que finalment (després de potencials edicions de quanitat dades o tipus) han canviat.
        log.trace(json.serialize(comanda.linies()));

        List<IProcessarLiniesComanda.ProcessarLiniesComandaReq> reqLinies = new ArrayList<IProcessarLiniesComanda.ProcessarLiniesComandaReq>();

        List<LiniaEDIResponse> linies = comanda.linies().get();
        List<LiniaComandaEDI> liniesEDIAProcessar = new ArrayList<LiniaComandaEDI>();
//        LiniaComandaEDIImpl.Builder liniaEDIAProcessarBuilder;

        String codiClient;
        if (comanda.codiClient().isEmpty())
            codiClient=comanda.comanda().codiClientAmes();
        else
            codiClient=comanda.codiClient().get();

        DadesComandaEDI comandaTotal = queryRepository.find(codiComanda).get();
        ComandaMissatgeEDI jsonComanda = comandaTotal.json();

        Optional<Linea> lineaDelEDI = jsonComanda.getLineas().stream()
                .filter(l -> l.getLA() != null && linies.get(0).codiArticleAmes().equals(l.getLA().getIdArticuloComprador()))
                .filter(l -> l.getLC() != null && comandaTotal.nad().equals(l.getLC().getCodigoConsignatario()))
                .findFirst();

        for (LiniaEDIResponse liniaComandaEDI : linies) {
            ProcessarLiniesComandaReqImpl.Builder processarLiniesComandaReqBuilder = ProcessarLiniesComandaReqImpl.builder();
//			if liniaComandaEDI.

            if (!liniaComandaEDI.codiComandaProcessat().isEmpty()) {
                KeyLiniaComandaImpl.Builder keyLinia = KeyLiniaComandaImpl.builder();
                keyLinia.comanda(liniaComandaEDI.codiComandaProcessat().get())
                        .numero(liniaComandaEDI.codiLiniaProcessat().get());
                processarLiniesComandaReqBuilder.clauLinia(keyLinia.build());
            } else
                processarLiniesComandaReqBuilder.clauLinia(Optional.empty());
            String codiLiniaClient;
//            if (!liniaComandaEDI.codiLiniaClient().map(String::isEmpty).orElse(true) )
//                ProcessarLiniesComandaReqBuilder.comandaClient(liniaComandaEDI.codiComandaClient().get() + "/" + liniaComandaEDI.codiLiniaClient().get());
//            else
//            if (codiClient.equals("234801"))
//                processarLiniesComandaReqBuilder.comandaClient(liniaComandaEDI.codiComandaClient().get()+"/"+liniaComandaEDI.codiLiniaClient().get());
//            else
                processarLiniesComandaReqBuilder.comandaClient(liniaComandaEDI.codiComandaClient().get());
            processarLiniesComandaReqBuilder.dataSolicitada(liniaComandaEDI.dataClient().get().toLocalDate())
                    .dataPrevistaSortida(liniaComandaEDI.dataAMES().get().toLocalDate());
            if (!liniaComandaEDI.dataMagatzem().isEmpty())
                processarLiniesComandaReqBuilder.dataPrevistaSortidaInterna(liniaComandaEDI.dataMagatzem().get().toLocalDate());
            processarLiniesComandaReqBuilder.quantitat((long) liniaComandaEDI.quantitat())
                    .tipus(liniaComandaEDI.tipus());

//              if (!lineaDelEDI.isEmpty()) {

            reqLinies.add(processarLiniesComandaReqBuilder.build());

//			if (liniaComandaEDI.processable() && ((liniaComandaEDI.quantitat()!=liniaComandaEDI.quantitatProcessat().get()) || liniaComandaEDI.codiComandaClient()!=liniaComandaEDI.codiComandaClientProcessat())) {
//            KeyArticleClientImpl.Builder keyArticleClient = KeyArticleClientImpl.builder();
//
//            keyArticleClient.clicod(codiClient).artint(codiArticle);
////            liniaEDIAProcessarBuilder = LiniaComandaEDIImpl.builder();
//            TipusLiniaComanda tipus = TipusLiniaComanda.ORIENTATIU;
//            if (liniaComandaEDI.tipus().equals("F"))
//                tipus = TipusLiniaComanda.FERM;
//            if (liniaComandaEDI.tipus().equals("O"))
//                tipus = TipusLiniaComanda.ORIENTATIU;
//            if (liniaComandaEDI.tipus().equals("I"))
//                tipus = TipusLiniaComanda.INVENT;
//
//            Date dataClient = new Date(liniaComandaEDI.dataClient().get().getTime());
//            Date dataAMES = new Date(liniaComandaEDI.dataAMES().get().getTime());
//            Optional<Date> dataMagatzem;
//            if (!liniaComandaEDI.dataMagatzem().isEmpty())
//                dataMagatzem = liniaComandaEDI.dataMagatzem();
//            else
//                dataMagatzem = Optional.empty();
//            liniaEDIAProcessarBuilder.articleClient(keyArticleClient.build())
//                    .dataClient(dataClient.toLocalDate())
//                    .dataAMES(dataAMES.toLocalDate());
//            if (!dataMagatzem.isEmpty())
//                liniaEDIAProcessarBuilder.dataMagatzem(dataMagatzem.get().toLocalDate());
//            liniaEDIAProcessarBuilder.quantitat(liniaComandaEDI.quantitat())
//                    .tipus(tipus)
//                    .usuari(RequestThread.nomUsuari());
//            //					.dataAMES(liniaComandaEDI.dataAMES().get())
//            //					.dataMagatzem(liniaComandaEDI.dataMagatzem().get())
//
//
//
//            liniesEDIAProcessar.add(liniaEDIAProcessarBuilder.build());
//			}
        }
//        KeyArticleClientImpl.Builder keyArticleClientBuilder = KeyArticleClientImpl.builder();
//        keyArticleClientBuilder.clicod(codiClient).artint(codiArticle);

//        json.
//        DadesXAlbaraImpl.Builder dadesXAlbaraBuilder =DadesXAlbaraImpl.builder();
//        dadesXAlbaraBuilder.idCompr(json.getCI().getIdComprador())
//                .idProve(json.getCI().getIdProveedor())
//                .idExped(json.getCI().getIdExpedidor())
//                .numCuen(json.getCI().getNumCuentaInternaProveedor())
//                        .

        log.trace("Envio a Processar AC: " + codiArticle+codiClient);
        ///*** DESCOMENTAR PER ENVIAR A PROCESSAR TEST @spurcet
        KeyArticleClientImpl.Builder keyArticleClientBuilder = KeyArticleClientImpl.builder();
        keyArticleClientBuilder.clicod(codiClient).artint(codiArticle);

//        Path filePath = Path.of("/home/docsapps/comercial/edis_indra/out/"+comanda.linies().get().get(0).codiComandaClient().get()+".json");
//        try {
//            Files.write(filePath, json.serialize(reqLinies).toString().getBytes(),
//                    StandardOpenOption.CREATE,
//                    StandardOpenOption.TRUNCATE_EXISTING);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }

        KeyArticleClient article = keyArticleClientBuilder.build();
        //processarLiniesComanda.executar(article, jsonComanda ,reqLinies);

        comandaEDIRepository.updateLiniaSetProcessada(codiComanda,codiArticle);

        int comandesRestants = queryRepository.listLinies(codiComanda).size();

        if (comandesRestants==0)
            comandaEDIRepository.updateComandaSetProcessada(codiComanda);

         //****/

//        return liniesEDIAProcessar;
//		if (queryRepository.ultimArticleDeComanda(codiComanda)) {
//			comandaEDIRepository.updateComandaSetProcessada(codiComanda);
//		}
//		comandaEDIRepository.deleteLiniesArticle(codiComanda,codiArticle);
    }

//	private boolean comandaProcessada(Long codiComanda) {
//		return queryRepository.comandaProcessada(codiComanda);
//	}

}

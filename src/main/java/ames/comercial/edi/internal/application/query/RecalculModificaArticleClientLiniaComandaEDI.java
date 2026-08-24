package ames.comercial.edi.internal.application.query;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.advantage.internal.response.ArticleClientInformacioComandaResponse;
import ames.comercial.edi.ComandesEDIException;
import ames.comercial.edi.internal.domain.DadesLiniaEDI;
import ames.comercial.edi.internal.infraestructure.query.QueryRepository;
import ames.comercial.edi.response.ComandaEDIResponse;
import ames.comercial.edi.response.ComandaEDIResponseImpl;
import ames.comercial.edi.response.LiniaEDIResponse;
import ames.comercial.edi.response.LiniaEDIResponseImpl;
import ames.comercial.shared.SharedExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecalculModificaArticleClientLiniaComandaEDI {

    @Autowired
    QueryRepository queryRepository;

    @Autowired
    IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda;

    public RecalculModificaArticleClientLiniaComandaEDI() {
//		this.comandaEDIRepository = comandaEDIRepository;
    }

    public ComandaEDIResponse executar(String newArticleClient, ComandaEDIResponse comanda) {
        ComandaEDIResponse outComandaEDIResponse;
        ComandaEDIResponseImpl.Builder outComandaEDIResponseBuilder = ComandaEDIResponseImpl.builder();
        List<LiniaEDIResponse> liniesRes = new ArrayList<LiniaEDIResponse>();
        LiniaEDIResponseImpl.Builder liniaEDIResponseBuilder = LiniaEDIResponseImpl.builder();
        List<LiniaEDIResponse> liniesReq = comanda.linies().get();
        String referencia = comanda.codiArticle().get();

        if (newArticleClient != null) {
            ArticleClientInformacioComandaResponse articleClient = obtenirArticleClientInformacioComanda.executar(newArticleClient)
                    .orElseThrow(() -> new SharedExceptions.ArticleClientNotFound(newArticleClient));

            if (!articleClient.codiClient().equals(comanda.codiClient().get()))
                throw new SharedExceptions.ArticleNoCorrespon(articleClient,comanda.codiClient().get());

            List<DadesLiniaEDI> linies = queryRepository.listLinies(comanda.comanda().codi().get());

//            if (linies.stream().anyMatch(dades -> articleClient.aclfab().equals(dades.codiArticleFab())))
//                throw new SharedExceptions.ArticleJaExistent(newArticleClient);

            outComandaEDIResponseBuilder.nomClientAmes(comanda.nomClientAmes())
                    .codiArticle(articleClient.artint())
                    .codiArticleFab(articleClient.aclfab())
                    .codiArticleAmes(articleClient.referencia())
                    .codiClient(comanda.codiClient())
                    .pathPDF(comanda.pathPDF())
                    .dataAcumulat(comanda.dataAcumulat())
                    .missatgeNumero(comanda.missatgeNumero())
                    .acumulatArticle(comanda.acumulatArticle())
                    .albaraAcumulat(comanda.albaraAcumulat())
                    .ultimAlbara(comanda.ultimAlbara())
                    .perfilDiesResta(comanda.perfilDiesResta())
                    .perfilDiesSortida(comanda.perfilDiesSortida())
                    .log(comanda.log())
                    .processable(true)
                    .stockAcumulat(comanda.stockAcumulat());

            for (LiniaEDIResponse liniaEDIResponse : liniesReq) {

                liniaEDIResponseBuilder.codi(liniaEDIResponse.codi())
                        .codiComanda(liniaEDIResponse.codiComanda())
                        .codiComandaClientProcessat(liniaEDIResponse.codiComandaClientProcessat())
                        .codiComandaClient(liniaEDIResponse.codiComandaClient())
                        .dataClient(liniaEDIResponse.dataClient())
                        .dataAMES(liniaEDIResponse.dataAMES())
                        .dataMagatzem(liniaEDIResponse.dataMagatzem())
                        .quantitat(liniaEDIResponse.quantitat())
                        .quantitatProcessat(liniaEDIResponse.quantitatProcessat())
                        .quantitatPendent(liniaEDIResponse.quantitatPendent())
                        .quantitatAcumulada(liniaEDIResponse.quantitatAcumulada()) // recalculem la quantitat acumulada
                        .tipus(liniaEDIResponse.tipus())
                        .observacions(liniaEDIResponse.observacions())
                        .comentarisAMES(liniaEDIResponse.comentarisAMES())
                        .alertesAMES(liniaEDIResponse.alertesAMES())
                        .ultimAlbara(liniaEDIResponse.ultimAlbara())
                        .status(liniaEDIResponse.status())
                        .codiArticle(articleClient.artint())
                        .codiArticleAmes(articleClient.referencia())
                        .processable(liniaEDIResponse.processable());
                liniesRes.add(liniaEDIResponseBuilder.build());
            }
        } else {
            throw new ComandesEDIException.ReferenciaNoValida();
        }

        outComandaEDIResponseBuilder.comanda(comanda.comanda()).stockArticle(comanda.stockArticle());

        outComandaEDIResponseBuilder.linies(Optional.of(liniesRes));

        return outComandaEDIResponseBuilder.build();
    }


}

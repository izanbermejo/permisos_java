package ames.comercial.comandes.internal.application.query;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.comandes.service.ICalcularDiaSortida;
import ames.comercial.server.RequestThread;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CalcularDiaSortidaArticleClient {

    ICalcularDiaSortida calcularDiaSortida;
    IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda;

    public CalcularDiaSortidaArticleClient(ICalcularDiaSortida calcularDiaSortida,
                                           IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda) {
        this.calcularDiaSortida = calcularDiaSortida;
        this.obtenirArticleClientInformacioComanda = obtenirArticleClientInformacioComanda;
    }

    public LocalDate executar(String articleClient, LocalDate dataSolicitada) {
        // Obtenció de l'informació de l'article-client
        var optInfoArticle = obtenirArticleClientInformacioComanda.executar(articleClient);
        if (optInfoArticle.isEmpty())
            return RequestThread.dateLocal();
        // Càlcul de la data de sortida
        var infoArticle = optInfoArticle.get();
        // Càlcul de la data
        var dataCalculada = calcularDiaSortida.executar(dataSolicitada, infoArticle.diesTransitClient(), infoArticle.diesSortida());
        // En cas que la data calculada sigui anterior al dia d'avui es retorna el dia d'avui i no la data calculada
        return dataCalculada.isBefore(RequestThread.dateLocal()) ? RequestThread.dateLocal() : dataCalculada;
    }

    public LocalDate executar(LocalDate dataSolicitada, int diesTransit, String diesSortida) {
        // Càlcul de la data
        var dataCalculada = calcularDiaSortida.executar(dataSolicitada, diesTransit, diesSortida);
        // En cas que la data calculada sigui anterior al dia d'avui es retorna el dia d'avui i no la data calculada
        return dataCalculada.isBefore(RequestThread.dateLocal()) ? RequestThread.dateLocal() : dataCalculada;
    }

}

package ames.comercial.comandes.internal.application.query;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.magatzem.ext.IObtenirDiesTransportMagatzems;
import ames.comercial.magatzem.ext.IObtenirMagatzems;
import ames.comercial.comandes.service.ICalcularDiaSortida;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.SharedExceptions.MagatzemNoExisteix;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class CalcularDataSortidaIntermitja {

    IObtenirMagatzems obtenirMagatzem;
    IObtenirDiesTransportMagatzems obtenirDiesTransportMagatzems;
    ICalcularDiaSortida calcularDiaSortida;
    IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda;

    public CalcularDataSortidaIntermitja (IObtenirMagatzems obtenirMagatzem, IObtenirDiesTransportMagatzems obtenirDiesTransportMagatzems,
                                          ICalcularDiaSortida calcularDiaSortida,
                                          IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda) {
        this.obtenirMagatzem = obtenirMagatzem;
        this.obtenirDiesTransportMagatzems = obtenirDiesTransportMagatzems;
        this.calcularDiaSortida = calcularDiaSortida;
        this.obtenirArticleClientInformacioComanda = obtenirArticleClientInformacioComanda;
    }

    public LocalDate executar(String articleClient, LocalDate dataSolicitada) {
        // Obtenció de l'informació de l'article-client
        var optInfoArticle = obtenirArticleClientInformacioComanda.executar(articleClient);
        if (optInfoArticle.isEmpty())
            return RequestThread.dateLocal();

        // En cas que no es necessite magatzem intermig es torna la mateixa data
        var infoArticle = optInfoArticle.get();
        if (!infoArticle.necessitaMagatzemIntermig())
            return avuiSiDataAnteriorAvui(dataSolicitada);

        // Obtenció del magatzem
        var magatzem = obtenirMagatzem.get(infoArticle.magatzemSortida()).orElseThrow(() -> new MagatzemNoExisteix(infoArticle.magatzemSortida()));

        // En cas de ser un magatzem plataforma es resten els dies de transport
        if (magatzem.isPlataforma())
            return avuiSiDataAnteriorAvui(calcularDiaSortida.executar(dataSolicitada, magatzem.diesTransport()));

        // En cas que no sigui magatzem plataforma s'obtenen els dies de transport entre els dos magatzems
        var diesEntreMagatzems = obtenirDiesTransportMagatzems.get(infoArticle.magatzemEntrada(), infoArticle.magatzemSortida());
        return avuiSiDataAnteriorAvui(calcularDiaSortida.executar(dataSolicitada, diesEntreMagatzems));
    }

    private LocalDate avuiSiDataAnteriorAvui(LocalDate data) {
        if (data.isBefore(RequestThread.dateLocal()))
            return RequestThread.dateLocal();
        return data;
    }

}

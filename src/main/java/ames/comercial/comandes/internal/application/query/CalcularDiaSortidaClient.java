package ames.comercial.comandes.internal.application.query;

import ames.comercial.advantage.internal.ObtenirClientAds;
import ames.comercial.comandes.service.ICalcularDiaSortida;
import ames.comercial.server.RequestThread;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Request;
import java.time.LocalDate;

@Component
public class CalcularDiaSortidaClient {

    ICalcularDiaSortida calcularDiaSortida;

    public CalcularDiaSortidaClient(ICalcularDiaSortida calcularDiaSortida) {
        this.calcularDiaSortida = calcularDiaSortida;
    }

    public LocalDate executar(String codiClient, LocalDate dataSolicitada) {
        var optClient = new ObtenirClientAds().get(codiClient);
        if (optClient.isEmpty())
            return RequestThread.dateLocal();
        // Càlcul de la data de sortida
        var infoClient = optClient.get();
        return calcularDiaSortida.executar(dataSolicitada, infoClient.diesTransitClient(), infoClient.diesSortida());
    }
}

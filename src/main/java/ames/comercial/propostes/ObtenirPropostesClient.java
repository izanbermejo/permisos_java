package ames.comercial.propostes;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.propostes.internal.application.query.CalcularPropostesClient;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesClients;
import ames.comercial.propostes.internal.provider.IProviderStockPropostes;
import ames.comercial.propostes.internal.service.ConstruirResumClients;
import ames.comercial.propostes.internal.service.ConstruirResumClientsNoFerm;
import ames.comercial.propostes.response.PropostesClientResponse;
import ames.comercial.propostes.response.PropostesClientResponseImpl;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.time.LocalDate;
import java.util.List;

@Component
public class ObtenirPropostesClient {

    @Autowired ObtenirRegistresPropostesClients obtenirRegistresPropostesClients;
    @Autowired IProviderStockPropostes providerStocksCalc;
    @Autowired CalcularPropostesClient calcularPropostesClient;
    @Autowired IObtenirClientAds obtenirClientAds;

    public PropostesClientResponse executar(String codiClient, String empresa, ObtenirPropostesClientRequest req) {
        var client = obtenirClientAds.get(codiClient).orElseThrow(() -> new ClientNoExisteix(codiClient));
        var listArticlesTraspas = obtenirRegistresPropostesClients.executar(codiClient, empresa, req.magatzem, req.dataPrevistaInici, req.dataPrevistaFi);
        var providerStock = providerStocksCalc.provide(List.of(codiClient));
        var propostesResp = calcularPropostesClient.executar(listArticlesTraspas, req.magatzem, providerStock);
        return PropostesClientResponseImpl.builder()
                .isAlbaraPerComanda(client.isAlbaraPerComanda())
                .isAlbaraPerPesa(client.isAlbaraPerPesa())
                .propostes(propostesResp.propostes())
                .propostesFerm(propostesResp.propostesFerm())
                .propostesInvent(propostesResp.propostesInvent())
                .propostesOrientatiu(propostesResp.propostesOrientatiu())
                .resumClient(new ConstruirResumClients().build(propostesResp.propostes(), List.of()).stream().findFirst())
                .resumClientFerm(new ConstruirResumClients().build(propostesResp.propostesFerm(), List.of()).stream().findFirst())
                .resumClientInvent(new ConstruirResumClientsNoFerm().build(propostesResp.propostesInvent()).stream().findFirst())
                .resumClientOrientatiu(new ConstruirResumClientsNoFerm().build(propostesResp.propostesOrientatiu()).stream().findFirst())
                .build();
    }

    public static class ObtenirPropostesClientRequest {
        @QueryParam("magatzem") String magatzem;
        @QueryParam("dataPrevistaInici") @DefaultValue("1900-01-01") LocalDate dataPrevistaInici;
        @QueryParam("dataPrevistaFi") LocalDate dataPrevistaFi;
    }

}

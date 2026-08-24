package ames.comercial.propostes;

import ames.comercial.cache.stocksatelit.StockSatelitCacheService;
import ames.comercial.propostes.internal.application.query.CalcularPropostesClient;
import ames.comercial.propostes.internal.application.query.CalcularPropostesClient.CalcularPropostesClientResponse;
import ames.comercial.propostes.internal.application.query.CalcularPropostesTraspas;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesClients;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesClients.RegArticlesPropostesClient;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse.RegArticlesTraspasPendents;
import ames.comercial.propostes.internal.provider.IProviderStockPropostes;
import ames.comercial.propostes.internal.service.ConstruirResumClients;
import ames.comercial.propostes.internal.service.ConstruirResumClientsNoFerm;
import ames.comercial.propostes.internal.service.ConstruirResumMagatzems;
import ames.comercial.propostes.response.ResumPropostesEntregaResponse;
import ames.comercial.propostes.response.ResumPropostesEntregaResponseImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@EnableAsync
public class ObtenirResumPropostesEntrega {

    @Autowired ObtenirRegistresPropostesClients obtenirRegistresPropostesClients;
    @Autowired ObtenirRegistresPropostesTraspas obtenirRegistresPropostesTraspas;
    @Autowired IProviderStockPropostes providerStocksCalc;
    @Autowired CalcularPropostesClient calcularPropostesClient;
    @Autowired CalcularPropostesTraspas calcularPropostesTraspas;
    @Autowired StockSatelitCacheService stockSatelitCacheService;

    public ResumPropostesEntregaResponse executar(ObtenirResumPropostesEntregaRequest req) {
        var registresCients = obtenirRegistresPropostesClients.executar(req.magatzem, req.dataPrevistaInici, req.dataPrevistaFi, req.responsables, req.transportista);
        var registresTraspas = obtenirRegistresPropostesTraspas.executar(req.magatzem, req.dataPrevistaInici, req.dataPrevistaFi, req.responsables, req.transportista);
        var providerStock = providerStocksCalc.provide(obtenirClients(registresCients, registresTraspas));
        CompletableFuture<CalcularPropostesClientResponse> clientFuture = CompletableFuture.supplyAsync(() ->
                calcularPropostesClient.executar(registresCients, req.magatzem, providerStock)
        );
        CompletableFuture<CalculPropostesTraspasResponse> traspasFuture = CompletableFuture.supplyAsync(() ->
                calcularPropostesTraspas.executar(registresTraspas, providerStock)
        );
        // Esperar que acabin totes dues tasques
        CompletableFuture.allOf(clientFuture, traspasFuture).join();

        try {
            var respostaPropostaClients = clientFuture.get();
            var respostaPropostaTraspas = traspasFuture.get();
            return ResumPropostesEntregaResponseImpl.builder()
                    .resumClient(new ConstruirResumClients().build(respostaPropostaClients.propostes(), List.of()))
                    .resumClientFerm(new ConstruirResumClients().build(respostaPropostaClients.propostesFerm(), List.of()))
                    .resumClientInvent(new ConstruirResumClientsNoFerm().build(respostaPropostaClients.propostesInvent()))
                    .resumClientOrientatiu(new ConstruirResumClientsNoFerm().build(respostaPropostaClients.propostesOrientatiu()))
                    .resumMagatzem(new ConstruirResumMagatzems().build(respostaPropostaTraspas.traspassos()))
                    .ultimRefreshCacheSatelits(stockSatelitCacheService.obtenirUltimRefresh())
                    .build();
        } catch (Exception e ) {
            throw new RuntimeException(e);
        }
    }

    private List<String> obtenirClients(List<RegArticlesPropostesClient> regClients, List<RegArticlesTraspasPendents> regTraspas) {
        return Stream.concat(
                        regClients.stream().map(RegArticlesPropostesClient::client),
                        regTraspas.stream().map(RegArticlesTraspasPendents::client))
                .collect(Collectors.toSet())
                .stream()
                .toList();
    }

    public static class ObtenirResumPropostesEntregaRequest {
        @QueryParam("magatzem") String magatzem;
        @QueryParam("dataPrevistaInici") @DefaultValue("1900-01-01") LocalDate dataPrevistaInici;
        @QueryParam("dataPrevistaFi") LocalDate dataPrevistaFi;
        @QueryParam("responsable") List<String> responsables;
        @QueryParam("transportista") @DefaultValue("") String transportista;
    }

}

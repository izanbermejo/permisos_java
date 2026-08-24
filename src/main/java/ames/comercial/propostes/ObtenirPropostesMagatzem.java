package ames.comercial.propostes;

import ames.comercial.magatzem.ext.IObtenirMagatzems;
import ames.comercial.propostes.internal.application.query.CalcularPropostesTraspas;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse.RegArticlesTraspasPendents;
import ames.comercial.propostes.internal.provider.IProviderStockPropostes;
import ames.comercial.propostes.response.PropostesTraspasResponse;
import ames.comercial.propostes.response.PropostesTraspasResponseImpl;
import ames.comercial.shared.SharedExceptions.MagatzemNoExisteix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.time.LocalDate;
import java.util.List;

@Component
public class ObtenirPropostesMagatzem {

    @Autowired IObtenirMagatzems obtenirMagatzemAds;
    @Autowired IProviderStockPropostes providerStocksCalc;
    @Autowired ObtenirRegistresPropostesTraspas obtenirRegistresPropostesTraspas;
    @Autowired CalcularPropostesTraspas calcularPropostesTraspas;

    public PropostesTraspasResponse executar(String codiMagatzemDesti, ObtenirPropostesMagatzemRequest req) {
        var magatzemDesti = obtenirMagatzemAds.get(codiMagatzemDesti).orElseThrow(() -> new MagatzemNoExisteix(codiMagatzemDesti));
        var registresTraspas = obtenirRegistresPropostesTraspas.executar(req.magatzem, codiMagatzemDesti, req.dataPrevistaInici, req.dataPrevistaFi);
        var providerStock = providerStocksCalc.provide(obtenirClients(registresTraspas));
        var propostesMagResp = calcularPropostesTraspas.executar(registresTraspas, providerStock);
        return PropostesTraspasResponseImpl.builder()
                .magatzemOrigen(req.magatzem)
                .magatzemDesti(codiMagatzemDesti)
                .isMagatzemDestiPlataforma(magatzemDesti.isPlataforma())
                .traspassos(propostesMagResp.traspassos())
                .build();
    }

    private List<String> obtenirClients (List<RegArticlesTraspasPendents> registresTraspas) {
        return registresTraspas.stream()
                .map(RegArticlesTraspasPendents::client)
                .distinct()
                .toList();
    }

    public static class ObtenirPropostesMagatzemRequest {
        @QueryParam("magatzem") String magatzem;
        @QueryParam("dataPrevistaInici") @DefaultValue("1900-01-01") LocalDate dataPrevistaInici;
        @QueryParam("dataPrevistaFi") LocalDate dataPrevistaFi;
    }

}

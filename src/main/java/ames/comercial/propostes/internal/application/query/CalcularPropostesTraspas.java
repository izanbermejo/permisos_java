package ames.comercial.propostes.internal.application.query;

import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse.DatesPrimeraLiniaTraspas;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse.InfoComandaPrimeraLiniaTraspas;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse.RegArticlesTraspasPendents;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.LiniaBreakpointTraspas;
import ames.comercial.propostes.internal.provider.IProviderStockPropostes.IProviderStockPropostesResponse;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CalcularPropostesTraspas {

    @Autowired ObtenirRegistresPropostesTraspas obtenirRegistresPropostesTraspas;

    public CalculPropostesTraspasResponse executar(List<RegArticlesTraspasPendents> listArticlesTraspas,
                                                   IProviderStockPropostesResponse stockProvider) {
        var result = buildTraspas(listArticlesTraspas, stockProvider);
        return CalculPropostesTraspasResponseImpl.builder()
                .traspassos(result)
                .build();
    }

    private List<RegArticlesTraspasPendents> buildTraspas(List<RegArticlesTraspasPendents> pendents, IProviderStockPropostesResponse stockProvider) {
        var result = new ArrayList<RegArticlesTraspasPendents>();
        for (var reg : pendents) {
            var articleClient = KeyArticleClient.of(reg.artint(), reg.clicod());
            var stockEntrega = stockProvider.stock(articleClient, reg.empresaEntrega(), reg.magEntrega());
            var stockOrigen = stockProvider.stock(articleClient, reg.empresaOrigen(), reg.magOrigen());
            var stockOrigenSatelit = stockProvider.stockSatelit(articleClient, reg.magOrigen());
            // El càlcul de la qtat màxima traspassable és la quantitat a enviar menys
            // l'stock del que es disposa al magatzem de destí
            var qtatTraspas = Math.max(reg.qtatSolicitada() - stockEntrega, 0);
            // La quantitat màxima a traspassar és el mínim entre l'stock a origen
            // i el màxim traspassable
            var qtatTraspassable = Math.min(qtatTraspas, stockOrigen);
            // Només s'afegeix en cas que hagi peces a traspassar
            if (qtatTraspas > 0) {
                result.add(RegArticlesTraspasPendentsImpl.builder()
                        .from(reg)
                        .stockOrigen(stockOrigen)
                        .stockOrigenSatelit(stockOrigenSatelit)
                        .stockDesti(stockEntrega)
                        .qtatTraspas(qtatTraspas)
                        .qtatTraspassable(qtatTraspassable)
                        .datesPrimeraLiniaTraspas(calcularDatesPrimeraLinia(reg.breakpoints(), stockEntrega))
                        .infoComandaPrimeraLinia(calcularInfoComandaPrimeraLinia(reg.breakpoints(), stockEntrega))
                        .build());
            }
        }
        return result;
    }

    // Amb les línies del grup acumulades per data i l'stock disponible al destí, la primera línia que
    // el traspàs ha de cobrir és la primera amb acumulat > stock del destí (l'stock cobreix les més
    // antigues). En retornem les 3 dates. Per als grups que arriben aquí sempre existeix (qtatTraspas > 0).
    private Optional<DatesPrimeraLiniaTraspas> calcularDatesPrimeraLinia(List<LiniaBreakpointTraspas> breakpoints, int stockDesti) {
        return breakpoints.stream()
                .filter(bp -> bp.acum() > stockDesti)
                .findFirst()
                .map(bp -> DatesPrimeraLiniaTraspasImpl.builder()
                        .dataSolicitada(bp.dataSolicitada())
                        .dataSortida(bp.dataSortida())
                        .dataSortidaInterna(bp.dataSortidaInterna())
                        .build());
    }

    // Informació de comanda de la mateixa primera línia (primera amb acumulat > stock del destí).
    private Optional<InfoComandaPrimeraLiniaTraspas> calcularInfoComandaPrimeraLinia(List<LiniaBreakpointTraspas> breakpoints, int stockDesti) {
        return breakpoints.stream()
                .filter(bp -> bp.acum() > stockDesti)
                .findFirst()
                .map(bp -> InfoComandaPrimeraLiniaTraspasImpl.builder()
                        .comanda(bp.comanda())
                        .comandaClient(bp.comandaClient())
                        .programa(bp.programa())
                        .build());
    }

}

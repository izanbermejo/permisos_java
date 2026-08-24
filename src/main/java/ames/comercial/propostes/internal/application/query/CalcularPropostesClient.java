package ames.comercial.propostes.internal.application.query;

import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesClients.RegArticlesPropostesClient;
import ames.comercial.propostes.internal.provider.IProviderStockPropostes.IProviderStockPropostesResponse;
import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CalcularPropostesClient {

    public CalcularPropostesClientResponse executar(List<RegArticlesPropostesClient> listArticlesTraspas, String magatzem, IProviderStockPropostesResponse stockProvider) {
        var result = buildPropostes(magatzem, listArticlesTraspas, stockProvider);
        return CalcularPropostesClientResponseImpl.builder()
                .propostes(result)
                .propostesFerm(result.stream().filter(l -> TipusLiniaComanda.FERM.equals(l.tipusLinia())).toList())
                .propostesInvent(result.stream().filter(l -> TipusLiniaComanda.INVENT.equals(l.tipusLinia())).toList())
                .propostesOrientatiu(result.stream().filter(l -> TipusLiniaComanda.ORIENTATIU.equals(l.tipusLinia())).toList())
                .build();
    }

    private List<RegArticlesPropostesClient> buildPropostes(String magatzem, List<RegArticlesPropostesClient> pendents, IProviderStockPropostesResponse providerStocks) {
        var result = new ArrayList<RegArticlesPropostesClient>();
        pendents.forEach(reg -> {
            var infoStock = providerStocks.stockServir(reg.articleClient(), reg.empresaEntrega(), magatzem, reg.qtatPendent());
            result.add(RegArticlesPropostesClientImpl.builder()
                    .from(reg)
                    .stockServir(infoStock.stockDisponibleServir())
                    .stockSatelit(infoStock.stockSatelit())
                    .necessitaStockSatelit(infoStock.isNecessitaSatelit())
                    .build());
        });
        return result;
    }

    @JsonDeserialize(builder = CalcularPropostesClientResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CalcularPropostesClientResponse {

        List<RegArticlesPropostesClient> propostes();
        List<RegArticlesPropostesClient> propostesFerm();
        List<RegArticlesPropostesClient> propostesInvent();
        List<RegArticlesPropostesClient> propostesOrientatiu();

        @JsonDeserialize(builder = ResumClientImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface ResumClient {
            String clientCodi();
            String clientNom();
            String empresa();
            boolean isBloquejat();
            LocalDate dataMin();
            boolean isHiHaPecesSatelit();
            boolean isNecessitaPecesDeSatelit();
            List<ResumTipusArticle> resumTipus();

            @Derived default String clientEmpresa() { return clientCodi() + empresa();}

            @Derived default int numLinies() { return resumTipus().stream().mapToInt(ResumTipusArticle::numLinies).sum(); }
            @Derived default int numLiniesServirTot() { return resumTipus().stream().mapToInt(ResumTipusArticle::numLiniesServirTot).sum(); }
            @Derived default int numLiniesServirParcial() { return resumTipus().stream().mapToInt(ResumTipusArticle::numLiniesServirParcial).sum(); }
            @Derived default int numLiniesServirCap() { return resumTipus().stream().mapToInt(ResumTipusArticle::numLiniesServirCap).sum(); }
            @Derived default boolean isHiHaComandesSotaMinim() { return resumTipus().stream().anyMatch(ResumTipusArticle::hiHaComandesSotaMinim); }
            @Derived default List<String> tipusArticles() { return resumTipus().stream().map(t -> t.tipus().toString()).collect(Collectors.toList()); }

            @JsonDeserialize(builder = ResumClientImpl.Builder.class)
            @Value.Style(typeImmutable = "*Impl")
            @Value.Immutable
            interface ResumTipusArticle {
                TipusArticleClient tipus();
                int numLinies();
                int numLiniesServirTot();
                int numLiniesServirParcial();
                int numLiniesServirCap();
                boolean hiHaComandesSotaMinim();
            }

        }

        @JsonDeserialize(builder = ResumClientNoFermImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface ResumClientNoFerm {
            String clientCodi();
            String clientNom();
            String empresa();
            boolean isBloquejat();
            LocalDate dataMin();
            boolean isHiHaPecesSatelit();
            int numLinies();
            @Derived default String clientEmpresa() { return clientCodi() + empresa();}
        }

    }

}

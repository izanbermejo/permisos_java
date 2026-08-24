package ames.comercial.propostes.internal.provider;

import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.util.List;

public interface IProviderStockPropostes {

    IProviderStockPropostesResponse provide(List<String> listClients);

    interface IProviderStockPropostesResponse {
        int stock(KeyArticleClient articleClient, String empresa, String magatzem);
        InformacioStockServir stockServir(KeyArticleClient articleClient, String empresa, String magatzem, int stockServir);
        int stockSatelit(KeyArticleClient articleClient, String magatzem);
    }

    @JsonDeserialize(builder = InformacioStockServirImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    interface InformacioStockServir {
        int stockDisponibleServir();
        int stockSatelit();
        int stockSobrantDespresServir();
        @Derived default boolean isNecessitaSatelit() { return stockSobrantDespresServir() <  stockSatelit();}

        static InformacioStockServir empty() {
            return InformacioStockServirImpl.builder()
                    .stockDisponibleServir(0)
                    .stockSatelit(0)
                    .stockSobrantDespresServir(0)
                    .build();
        }
    }

}

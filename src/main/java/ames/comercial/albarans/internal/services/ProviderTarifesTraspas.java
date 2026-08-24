package ames.comercial.albarans.internal.services;

import ames.comercial.server.exception.AppException;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tarifes amb què es valoren les línies dels albarans de traspàs: la tarifa AMES per als traspassos que es
 * facturen i la de client per a la resta (veure {@link PreusProviderSelector}).
 * <p>
 * La comparteixen la previsualització de la proposta i la creació dels albarans, de manera que el preu que
 * es mostra a la proposta i el que acaba a la línia d'albarà surten del mateix lloc i no poden divergir.
 */
@Component
public class ProviderTarifesTraspas {

    @Autowired PreusProviderSelector preusProvider;

    public Tarifes provide(Collection<KeyArticleClient> articleClients) {
        return new Tarifes(Set.copyOf(articleClients));
    }

    public class Tarifes {

        private final Set<KeyArticleClient> articleClients;
        private final Map<Boolean, Tarifa> tarifes = new HashMap<>();

        private Tarifes(Set<KeyArticleClient> articleClients) {
            this.articleClients = articleClients;
        }

        /**
         * Preu amb què es valorarà la peça segons si el traspàs es factura. Zero (amb la divisa comuna de la
         * tarifa) per a les peces que no tenen preu a la tarifa que s'aplica.
         */
        public Preu preu(boolean isFacturable, KeyArticleClient articleClient) {
            return tarifes.computeIfAbsent(isFacturable, this::carregar).preu(articleClient);
        }

        // Com que una mateixa proposta pot tenir albarans amb canvi d'empresa i sense, cada tarifa es
        // carrega només quan algun albarà la necessita.
        private Tarifa carregar(boolean isFacturable) {
            var preus = preusProvider.provide(isFacturable, articleClients);
            var divisaComuna = preus.values().stream()
                    .findFirst()
                    .map(Preu::divisa)
                    .map(Divisa::base)
                    .orElseThrow(() -> new AppException("No hi han preus per determinar la divisa"));
            return new Tarifa(preus, divisaComuna);
        }

    }

    /** Preus d'una tarifa (AMES o de client) amb la divisa comuna que s'aplica als articles que no en tenen */
    private record Tarifa(Map<KeyArticleClient, Preu> preus, Divisa divisa) {
        Preu preu(KeyArticleClient articleClient) {
            return preus.getOrDefault(articleClient, Preu.of(BigDecimal.ZERO, divisa));
        }
    }

}

package ames.comercial.propostes.internal.application.query;

import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesClients.RegArticlesPropostesClient;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Recalcula l'import net i el pes d'una línia de proposta per a una quantitat a servir concreta.
 * <p>
 * El càlcul es fa al servidor (amb la mateixa lògica que la resta de la proposta,
 * {@link RegArticlesPropostesClient#calcularImportNet} / {@link RegArticlesPropostesClient#calcularPes})
 * quan l'usuari edita la quantitat de l'albarà, de manera que el frontend no ha de replicar cap fórmula.
 */
@Component
public class RecalcularImportPesLinia {

    public RecalculLiniaResponse executar(RecalculLiniaRequest request) {
        return RecalculLiniaResponseImpl.builder()
                .importNet(RegArticlesPropostesClient.calcularImportNet(request.preu(), request.descompte(), request.quantitat()))
                .pes(RegArticlesPropostesClient.calcularPes(request.pesUnitari(), request.quantitat()))
                .build();
    }

    @JsonDeserialize(builder = RecalculLiniaRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface RecalculLiniaRequest {
        Preu preu();
        BigDecimal descompte();
        BigDecimal pesUnitari();
        long quantitat();
    }

    @JsonDeserialize(builder = RecalculLiniaResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface RecalculLiniaResponse {
        BigDecimal importNet();
        BigDecimal pes();
    }

}

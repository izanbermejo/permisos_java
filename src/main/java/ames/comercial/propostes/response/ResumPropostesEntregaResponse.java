package ames.comercial.propostes.response;

import ames.comercial.propostes.internal.application.query.CalcularPropostesClient.CalcularPropostesClientResponse.ResumClient;
import ames.comercial.propostes.internal.application.query.CalcularPropostesClient.CalcularPropostesClientResponse.ResumClientNoFerm;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse.ResumMagatzem;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = ResumPropostesEntregaResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface ResumPropostesEntregaResponse {

    List<ResumClient> resumClient();
    List<ResumClient> resumClientFerm();
    List<ResumClientNoFerm> resumClientInvent();
    List<ResumClientNoFerm> resumClientOrientatiu();

    List<ResumMagatzem> resumMagatzem();

    Optional<LocalDateTime> ultimRefreshCacheSatelits();

    @Derived
    default int numClients () { return resumClient().size(); }

    @Derived
    default int numClientsFerm () { return resumClientFerm().size(); }

    @Derived
    default int numClientsInvent () { return resumClientInvent().size(); }

    @Derived
    default int numClientsOrientatiu () { return resumClientOrientatiu().size(); }

    @Derived
    default int numTraspassos () { return resumMagatzem().size(); }

}

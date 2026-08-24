package ames.comercial.propostes.response;

import ames.comercial.propostes.internal.application.query.CalcularPropostesClient.CalcularPropostesClientResponse.ResumClient;
import ames.comercial.propostes.internal.application.query.CalcularPropostesClient.CalcularPropostesClientResponse.ResumClientNoFerm;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesClients.RegArticlesPropostesClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Optional;

@JsonDeserialize(builder = PropostesClientResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface PropostesClientResponse {

    boolean isAlbaraPerComanda();
    boolean isAlbaraPerPesa();
    List<RegArticlesPropostesClient> propostes();
    List<RegArticlesPropostesClient> propostesFerm();
    List<RegArticlesPropostesClient> propostesInvent();
    List<RegArticlesPropostesClient> propostesOrientatiu();

    Optional<ResumClient> resumClient();
    Optional<ResumClient> resumClientFerm();
    Optional<ResumClientNoFerm> resumClientInvent();
    Optional<ResumClientNoFerm> resumClientOrientatiu();

}

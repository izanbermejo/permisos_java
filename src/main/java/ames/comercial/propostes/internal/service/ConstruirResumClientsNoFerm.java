package ames.comercial.propostes.internal.service;

import ames.comercial.propostes.internal.application.query.CalcularPropostesClient.CalcularPropostesClientResponse.ResumClientNoFerm;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesClients.RegArticlesPropostesClient;
import ames.comercial.propostes.internal.application.query.ResumClientNoFermImpl;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConstruirResumClientsNoFerm {

    public List<ResumClientNoFerm> build(List<RegArticlesPropostesClient> propClients) {
        Map<String, List<RegArticlesPropostesClient>> agrupacioClient = propClients.stream()
                .collect(Collectors.groupingBy(p -> p.client() + "|" + p.empresaEntrega()));

        // Recorregut de l'agrupació per client per fer el resum de cada client
        return agrupacioClient.entrySet().stream()
            // Construcció del resum
            .map(entry -> {
                String[] parts = entry.getKey().split("\\|");
                String client = parts[0];
                String empresa = parts[1];

                List<RegArticlesPropostesClient> liniesClient = entry.getValue();
                // Data mínima
                var dataMinima = liniesClient.stream().map(RegArticlesPropostesClient::dataSortida)
                        .min(LocalDate::compareTo).orElseThrow();
                // Peces en estants sate·lits
                var pecesSatelit = liniesClient.stream().anyMatch(r -> r.stockSatelit() > 0);

                RegArticlesPropostesClient propostaClient = entry.getValue().get(0);
                return ResumClientNoFermImpl.builder()
                        .clientCodi(client)
                        .clientNom(propostaClient.clientNom())
                        .empresa(empresa)
                        .isBloquejat(propostaClient.isClientBloquejat())
                        .dataMin(dataMinima)
                        .isHiHaPecesSatelit(pecesSatelit)
                        .numLinies(liniesClient.size())
                        .build();
        }).sorted(Comparator.comparing(ResumClientNoFerm::clientCodi))
          .collect(Collectors.toList());
    }

}

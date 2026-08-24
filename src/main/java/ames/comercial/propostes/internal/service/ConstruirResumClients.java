package ames.comercial.propostes.internal.service;

import ames.comercial.propostes.internal.application.query.CalcularPropostesClient.CalcularPropostesClientResponse.ResumClient;
import ames.comercial.propostes.internal.application.query.CalcularPropostesClient.CalcularPropostesClientResponse.ResumClient.ResumTipusArticle;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesClients.RegArticlesPropostesClient;
import ames.comercial.propostes.internal.application.query.ResumClientImpl;
import ames.comercial.propostes.internal.application.query.ResumTipusArticleImpl;
import ames.comercial.shared.TipusArticleClient;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ConstruirResumClients {

    public List<ResumClient> build(List<RegArticlesPropostesClient> propClients, List<TipusArticleClient> tipusArticleExcloure) {
        Map<String, List<RegArticlesPropostesClient>> agrupacioClient = propClients.stream()
                .collect(Collectors.groupingBy(p -> p.client() + "|" + p.empresaEntrega()));

        // Recorregut de l'agrupació per client per fer el resum de cada client
        return agrupacioClient.entrySet().stream()
            .filter(entry -> {
                List<RegArticlesPropostesClient> linies = entry.getValue();
                Set<TipusArticleClient> tipusDelGrup = linies.stream()
                        .map(RegArticlesPropostesClient::tipusArticleClient)
                        .collect(Collectors.toSet());
                // Si al menys un tipus NO està en el filtre, mantenim el grup
                return tipusDelGrup.stream()
                        .anyMatch(tipus -> !tipusArticleExcloure.contains(tipus));
            })
            // Construcció del resum
            .map(entry -> {
                String[] parts = entry.getKey().split("\\|");
                String client = parts[0];
                String empresa = parts[1];

                List<RegArticlesPropostesClient> liniesClient = entry.getValue();

                // Dins de cada client es fa l'agrupació per tipus
                Map<TipusArticleClient, List<RegArticlesPropostesClient>> agrupacioTipus = liniesClient.stream()
                        .collect(Collectors.groupingBy(RegArticlesPropostesClient::tipusArticleClient));
                var resumTipus = construirResumTipusArticle(agrupacioTipus);

                // Data mínima
                var dataMinima = liniesClient.stream().map(RegArticlesPropostesClient::dataSortida)
                        .min(LocalDate::compareTo).orElseThrow();
                // Peces en estants sate·lits
                var pecesSatelit = liniesClient.stream().anyMatch(r -> r.stockSatelit() > 0);
                // Necessitat d'agafar peces d'estants satel·lits
                var necessitaPecesSatelit = liniesClient.stream().anyMatch(RegArticlesPropostesClient::necessitaStockSatelit);

                RegArticlesPropostesClient propostaClient = entry.getValue().get(0);
                return ResumClientImpl.builder()
                        .clientCodi(client)
                        .clientNom(propostaClient.clientNom())
                        .empresa(empresa)
                        .isBloquejat(propostaClient.isClientBloquejat())
                        .dataMin(dataMinima)
                        .isHiHaPecesSatelit(pecesSatelit)
                        .isNecessitaPecesDeSatelit(necessitaPecesSatelit)
                        .resumTipus(resumTipus)
                        .build();
        }).sorted(Comparator.comparing(ResumClient::clientCodi))
          .collect(Collectors.toList());
    }

    private List<ResumTipusArticle> construirResumTipusArticle (Map<TipusArticleClient, List<RegArticlesPropostesClient>> agrupacioTipus) {
        return agrupacioTipus.entrySet().stream()
                .map(tipusEntry -> {
                    TipusArticleClient tipus = tipusEntry.getKey();
                    List<RegArticlesPropostesClient> liniesTipus = tipusEntry.getValue();

                    long numLinies = liniesTipus.size();
                    long numLiniesCap = liniesTipus.stream()
                            .filter(l -> l.stockAlbara() == 0)
                            .count();
                    long numLiniesTot = liniesTipus.stream()
                            .filter(l -> l.stockAlbara()>=l.qtatPendent())
                            .count();
                    long numLiniesParcial = numLinies - numLiniesCap - numLiniesTot;
                    boolean hiHaLiniesSotaMinim = liniesTipus.stream().anyMatch(RegArticlesPropostesClient::isComandaNormalitzatSotaMinim);

                    return ResumTipusArticleImpl.builder()
                            .tipus(tipus)
                            .numLinies((int) numLinies)
                            .numLiniesServirTot((int)numLiniesTot)
                            .numLiniesServirCap((int)numLiniesCap)
                            .numLiniesServirParcial((int)numLiniesParcial)
                            .hiHaComandesSotaMinim(hiHaLiniesSotaMinim)
                            .build();
                })
                .collect(Collectors.toList());
    }

}

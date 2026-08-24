package ames.comercial.inventari.internal.application.query;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.advantage.internal.response.ArticleClientInformacioComandaResponse;
import ames.comercial.inventari.internal.application.query.ObtenirMoviments.FiltreMoviment;
import ames.comercial.inventari.internal.application.query.ObtenirMoviments.ObtenirMovimentsResponse;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ObtenirHistoric {

    @Autowired NamedParameterJdbcTemplate jdbcAmes;
    @Autowired ObtenirMoviments obtenirMoviments;
    @Autowired IObtenirArticleClientInformacioComanda obtenirArticleClientInfoComanda;

    public ObtenirHistoricResponse executar(KeyArticleClient articleClient, LocalDate dataInici, LocalDate dataFi) {
        return executar(articleClient, dataInici, dataFi, Optional.empty(), Optional.empty(), List.of());
    }

    public ObtenirHistoricResponse executar(
            KeyArticleClient articleClient,
            LocalDate dataInici, LocalDate dataFi,
            Optional<String> empresa, Optional<String> magatzem,
            List<FiltreMoviment> filtresMoviment) {
        var params = new MapSqlParameterSource("artint", articleClient.artint())
                .addValue("clicod", articleClient.clicod());

        var sqlCombos = """
                SELECT
                    m.empresa,
                    m.magatzem,
                    MIN(m.data)               AS primer_moviment,
                    MAX(m.data)               AS darrer_moviment,
                    COALESCE(MAX(f.stock), 0) AS stock
                FROM inventari.moviment m
                LEFT JOIN inventari.fitxa f ON
                    f.artint   = m.artint   AND
                    f.clicod   = m.clicod   AND
                    f.empresa  = m.empresa  AND
                    f.magatzem = m.magatzem
                WHERE m.artint = :artint AND m.clicod = :clicod
                GROUP BY m.empresa, m.magatzem
                ORDER BY MAX(m.data) DESC
                """;

        record ComboRow(String empresa, String magatzem, LocalDate primerMoviment, LocalDate darrerMoviment, long stock) {}

        var combos = jdbcAmes.query(sqlCombos, params, (rs, i) -> new ComboRow(
                rs.getString("empresa"),
                rs.getString("magatzem"),
                rs.getDate("primer_moviment").toLocalDate(),
                rs.getDate("darrer_moviment").toLocalDate(),
                rs.getLong("stock")
        ));

        var optInfo = obtenirArticleClientInfoComanda.executar(articleClient);
        var aclfab = optInfo.map(ArticleClientInformacioComandaResponse::aclfab).filter(s -> !s.isBlank());
        var referencia = optInfo.map(ArticleClientInformacioComandaResponse::referencia).filter(s -> !s.isBlank());
        var nomClient = optInfo.map(ArticleClientInformacioComandaResponse::nomClient).filter(s -> !s.isBlank());
        var formaEnviamentHabitual = optInfo.map(ArticleClientInformacioComandaResponse::formaEnviament);
        var incotermHabitual = optInfo.map(ArticleClientInformacioComandaResponse::incoterm);
        var destiHabitual = optInfo.map(ArticleClientInformacioComandaResponse::desti).filter(s -> !s.isBlank());
        var isTePesesSatelit = optInfo.map(ArticleClientInformacioComandaResponse::isTePesesSatelit).orElse(false);

        if (combos.isEmpty()) {
            return ObtenirHistoricResponseImpl.builder()
                    .empreses(List.of())
                    .empresaSeleccionada("")
                    .magatzemSeleccionat("")
                    .movimentsResponse(ObtenirMovimentsResponseImpl.builder()
                            .moviments(List.of())
                            .existenciaInicial(0L)
                            .build())
                    .aclfab(aclfab)
                    .referencia(referencia)
                    .nomClient(nomClient)
                    .formaEnviamentHabitual(formaEnviamentHabitual)
                    .incotermHabitual(incotermHabitual)
                    .destiHabitual(destiHabitual)
                    .isTePesesSatelit(isTePesesSatelit)
                    .build();
        }

        // Default = first combo (most recent darrer_moviment globally)
        var defaultCombo = combos.get(0);

        // Group by empresa preserving order (ORDER BY ensures most-recent empresa comes first)
        var magatzemsByEmpresa = new LinkedHashMap<String, List<ComboRow>>();
        for (var c : combos) {
            magatzemsByEmpresa.computeIfAbsent(c.empresa(), k -> new ArrayList<>()).add(c);
        }

        List<ItemEmpresaHistoric> empreses = magatzemsByEmpresa.entrySet().stream().map(entry -> {
            var mags = entry.getValue();
            var primer = mags.stream().map(ComboRow::primerMoviment).min(Comparator.naturalOrder()).get();
            var darrer = mags.stream().map(ComboRow::darrerMoviment).max(Comparator.naturalOrder()).get();
            List<ItemMagatzemHistoric> magatzems = mags.stream()
                    .map(m -> (ItemMagatzemHistoric) ItemMagatzemHistoricImpl.builder()
                            .magatzem(m.magatzem())
                            .stock(m.stock())
                            .primerMoviment(m.primerMoviment())
                            .darrerMoviment(m.darrerMoviment())
                            .build())
                    .collect(Collectors.toList());
            return (ItemEmpresaHistoric) ItemEmpresaHistoricImpl.builder()
                    .empresa(entry.getKey())
                    .primerMoviment(primer)
                    .darrerMoviment(darrer)
                    .magatzems(magatzems)
                    .build();
        }).collect(Collectors.toList());

        String empresaSeleccionada = empresa.orElse(defaultCombo.empresa());
        String magatzemSeleccionat = magatzem.orElse(defaultCombo.magatzem());

        var movimentsRequest = ObtenirMovimentsRequestImpl.builder()
                .articleClient(articleClient)
                .empresa(empresaSeleccionada)
                .magatzem(magatzemSeleccionat)
                .dataInici(dataInici)
                .dataFi(dataFi)
                .isOrdreAscendent(true)
                .filtresMoviment(filtresMoviment)
                .build();
        ObtenirMovimentsResponse movimentsResponse = obtenirMoviments.executar(movimentsRequest);

        return ObtenirHistoricResponseImpl.builder()
                .empreses(empreses)
                .empresaSeleccionada(empresaSeleccionada)
                .magatzemSeleccionat(magatzemSeleccionat)
                .movimentsResponse(movimentsResponse)
                .aclfab(aclfab)
                .referencia(referencia)
                .nomClient(nomClient)
                .formaEnviamentHabitual(formaEnviamentHabitual)
                .incotermHabitual(incotermHabitual)
                .destiHabitual(destiHabitual)
                .isTePesesSatelit(isTePesesSatelit)
                .build();
    }

    @JsonDeserialize(builder = ItemMagatzemHistoricImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ItemMagatzemHistoric {
        String magatzem();
        Long stock();
        LocalDate primerMoviment();
        LocalDate darrerMoviment();
    }

    @JsonDeserialize(builder = ItemEmpresaHistoricImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ItemEmpresaHistoric {
        String empresa();
        LocalDate primerMoviment();
        LocalDate darrerMoviment();
        List<ItemMagatzemHistoric> magatzems();
    }

    @JsonDeserialize(builder = ObtenirHistoricResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirHistoricResponse {
        List<ItemEmpresaHistoric> empreses();
        String empresaSeleccionada();
        String magatzemSeleccionat();
        ObtenirMovimentsResponse movimentsResponse();
        Optional<String> aclfab();
        Optional<String> referencia();
        Optional<String> nomClient();
        Optional<FormaEnviament> formaEnviamentHabitual();
        Optional<Incoterm> incotermHabitual();
        Optional<String> destiHabitual();
        boolean isTePesesSatelit();

        @Value.Derived
        default long stockTotal() {
            return empreses().stream()
                    .flatMap(e -> e.magatzems().stream())
                    .mapToLong(ItemMagatzemHistoric::stock)
                    .sum();
        }
    }

}

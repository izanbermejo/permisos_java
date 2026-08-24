package ames.comercial.comandes.internal.application.query;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.internal.application.query.ObtenirComandaEspecial.ObtenirComandaEspecialResponse.ArticlesComandaResponse;
import ames.comercial.comandes.internal.application.query.ObtenirComandaEspecial.ObtenirComandaEspecialResponse.DadesEnviamentJustificantEspecialResponse;
import ames.comercial.comandes.internal.domain.comanda.DadesEnviamentJustificant;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.response.InformacioEnviamentResponse;
import ames.comercial.comandes.service.IProviderPes;
import ames.comercial.comandes.service.ProviderPes.IProviderPesResponse;
import ames.comercial.shared.Adresa;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ames.comercial.shared.Numbers.decimal;

@Service
public class ObtenirComandaEspecial {

    @Autowired ComandaRepository comandaRepo;
    @Autowired JdbcTemplate jdbc;
    @Autowired IProviderPes providerPes;

    public ObtenirComandaEspecialResponse executar (long codi) {
        // Obtenció de la comanda
        var comanda = comandaRepo.find(codi).orElseThrow(ComandaNoExisteix::new);
        // Obtenció dels articles de la comanda pendent (PostgreSQL)
        var rowsArticlesPendents = getRowsArticlesComanda(codi);
        // Obtenció dels pesos (a través del provider)
        var pesos = providerPes.provide(rowsArticlesPendents.stream().map(r -> r.articleClient.artint()).collect(Collectors.toSet()));
        return ObtenirComandaEspecialResponseImpl.builder()
                .comanda(comanda.codi())
                .empresa(comanda.dades().empresa())
                .codiClient(comanda.dades().client())
                .comandaClient(comanda.informacioClient().identificador())
                .programa(comanda.informacioClient().programa())
                .adresa(comanda.adresa())
                .data(comanda.dades().dataAlta())
                .informacioEnviament(InformacioEnviamentResponse.of(comanda.informacioEnviament()))
                .listArticles(buildArticlesComanda(rowsArticlesPendents, pesos))
                .dadesEnviamentJustificant(DadesEnviamentJustificantEspecialResponse.of(comanda.dadesEnviamentJustificant()))
                .build();
    }

    private List<ArticlesComandaResponse> buildArticlesComanda(List<RowArticlesComanda> rowArticlesComandes, IProviderPesResponse pesResponse) {
        var resultat = new ArrayList<ArticlesComandaResponse>();
        for (RowArticlesComanda row : rowArticlesComandes) {
            var resp = ArticlesComandaResponseImpl.builder()
                    .articleClient(row.articleClient)
                    .article(row.article)
                    .referencia(row.referencia)
                    .denominacio(row.denominacio)
                    .numPreus(row.numPreus)
                    .preu(row.preu)
                    .stock(row.stock)
                    .quantitatPendent(row.quantitatPendent)
                    .quantitatTotal(row.quantitatTotal)
                    .pesPesa(pesResponse.pes(row.articleClient.artint()))
                    .preuPendent(row.preuPendent)
                    .preuTotal(row.preuTotal)
                    .build();
            resultat.add(resp);
        }
        return resultat.stream().sorted(Comparator.comparing(ArticlesComandaResponse::referencia)).toList();
    }

    private List<RowArticlesComanda> getRowsArticlesComanda(long comanda) {
        return jdbc.query("""
                WITH stock_article AS (
                    SELECT artint, clicod, SUM(stock) AS stock
                    FROM inventari.fitxa
                    GROUP BY artint, clicod
                )
                SELECT
                    lc.artint,
                    lc.clicod,
                    ac.codi_fabrica,
                    ac.referencia,
                    ac.denominacio,
                    sa.stock,
                    SUM(lc.quantitat_pendent) AS quantitatPendent,
                    SUM(lc.quantitat) AS quantitatTotal,
                    COUNT(DISTINCT (lc.preu, lc.divisa)) AS numPreus,
                    CASE
                        WHEN COUNT(DISTINCT (lc.preu, lc.divisa)) = 1 THEN MIN(lc.preu)
                        ELSE NULL
                    END AS preu,
                    CASE
                        WHEN COUNT(DISTINCT (lc.preu, lc.divisa)) = 1 THEN MIN(lc.divisa)
                        ELSE NULL
                    END AS divisa,
                    SUM(
                        lc.quantitat_pendent *
                        CASE
                            WHEN lc.divisa LIKE '%\\%%' THEN lc.preu * (1 - COALESCE((lc.dades_calcul ->> 'descompte')::numeric, 0)/100) / 100
                            ELSE lc.preu * (1 - COALESCE((lc.dades_calcul ->> 'descompte')::numeric, 0)/100)
                        END
                    ) AS preuPendent,
                    SUM(
                        lc.quantitat *
                        CASE
                            WHEN lc.divisa LIKE '%\\%%' THEN lc.preu * (1 - COALESCE((lc.dades_calcul ->> 'descompte')::numeric, 0)/100) / 100
                            ELSE lc.preu * (1 - COALESCE((lc.dades_calcul ->> 'descompte')::numeric, 0)/100)
                        END
                    ) AS preuTotal
                FROM comandes.linia_comanda lc
                LEFT JOIN cache.cache_article_client ac
                       ON ac.artint = lc.artint
                      AND ac.clicod = lc.clicod
                LEFT JOIN stock_article sa
                       ON sa.artint = lc.artint
                      AND sa.clicod = lc.clicod
                WHERE lc.actual
                  AND lc.comanda = ?
                GROUP BY
                    lc.artint,
                    lc.clicod,
                    ac.codi_fabrica,
                    ac.referencia,
                    ac.denominacio,
                    sa.stock;
            """,
        (rs, rowNum) -> {
            var numPreus = rs.getLong("numPreus");
            Optional<Preu> optPreu = numPreus == 1 ? Optional.of(Preu.of(rs.getBigDecimal("preu"), rs.getString("divisa"))) : Optional.empty();
            return new RowArticlesComanda(
                KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")),
                rs.getLong("quantitatPendent"),
                rs.getLong("quantitatTotal"),
                rs.getString("codi_fabrica"),
                rs.getString("referencia"),
                rs.getString("denominacio"),
                rs.getLong("stock"),
                rs.getBigDecimal("preutotal"),
                rs.getBigDecimal("preupendent"),
                numPreus,
                optPreu
            );
        },
        comanda);
    }

    private record RowArticlesComanda (KeyArticleClient articleClient, long quantitatPendent, long quantitatTotal,
                                       String article, String referencia, String denominacio, long stock,
                                       BigDecimal preuTotal, BigDecimal preuPendent, long numPreus, Optional<Preu> preu) {};

    @JsonDeserialize(builder = ObtenirComandaEspecialResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirComandaEspecialResponse {
        long comanda();
        String empresa();
        String codiClient();
        String comandaClient();
        String programa();
        Adresa adresa();
        LocalDate data();
        InformacioEnviamentResponse informacioEnviament();
        List<ArticlesComandaResponse> listArticles();
        Optional<DadesEnviamentJustificantEspecialResponse> dadesEnviamentJustificant();

        @Derived default BigDecimal pesTotal() { return listArticles().stream().map(ArticlesComandaResponse::pesTotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
        @Derived default BigDecimal pesPendent() { return listArticles().stream().map(ArticlesComandaResponse::pesPendent).reduce(BigDecimal.ZERO, BigDecimal::add); }
        @Derived default BigDecimal preuTotal() { return listArticles().stream().map(ArticlesComandaResponse::preuTotal).reduce(BigDecimal.ZERO, BigDecimal::add); }
        @Derived default BigDecimal preuPendent() { return listArticles().stream().map(ArticlesComandaResponse::preuPendent).reduce(BigDecimal.ZERO, BigDecimal::add); }
        @Derived
        default Divisa divisa() {
            return listArticles().stream()
                    .map(ArticlesComandaResponse::divisa)
                    .flatMap(Optional::stream)
                    .findFirst()
                    .orElse(Divisa.EURO);
        }

        @JsonDeserialize(builder = LiniaComandaResponseImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface ArticlesComandaResponse {
            KeyArticleClient articleClient();
            String article();
            String referencia();
            String denominacio();
            long numPreus();
            Optional<Preu> preu();
            long stock();
            long quantitatPendent();
            long quantitatTotal();
            BigDecimal pesPesa();
            BigDecimal preuTotal();
            BigDecimal preuPendent();

            @Derived default BigDecimal pesTotal() {
                return pesPesa()
                    .multiply(decimal(quantitatTotal()))
                    .divide(decimal(1000), 2, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
            }

            @Derived default BigDecimal pesPendent() {
                return pesPesa()
                        .multiply(decimal(quantitatPendent()))
                        .divide(decimal(1000), 2, RoundingMode.HALF_UP)
                        .setScale(2, RoundingMode.HALF_UP);
            }

            @Derived
            default Optional<Divisa> divisa(){
                return preu().map(preu -> preu.divisa().base());
            }
        }

        @JsonDeserialize(builder = DadesEnviamentJustificantEspecialResponseImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface DadesEnviamentJustificantEspecialResponse {
            String to();
            String cc();
            String assumpte();
            String missatge();
            LocalDateTime data();
            String usuari();
            List<String> adjunts();

            static Optional<DadesEnviamentJustificantEspecialResponse> of(Optional<DadesEnviamentJustificant> optDades) {
                if (optDades.isEmpty())
                    return Optional.empty();
                var dades = optDades.get();
                return Optional.of(DadesEnviamentJustificantEspecialResponseImpl.builder()
                        .to(dades.to())
                        .cc(dades.cc())
                        .assumpte(dades.assumpte())
                        .missatge(dades.missatge())
                        .data(dades.data())
                        .usuari(dades.usuari())
                        .adjunts(dades.adjunts())
                        .build());
            }
        }
    }

}

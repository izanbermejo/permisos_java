package ames.comercial.inventari.internal.application.query;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.inventari.internal.application.query.extractor.ItemMovimentExtractor;
import ames.comercial.inventari.internal.domain.fitxa.KeyFitxa;
import ames.comercial.inventari.internal.domain.moviment.TipusMoviment;
import ames.comercial.inventari.internal.infraestructure.fitxa.FitxaRepository;
import ames.comercial.server.Json;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class ObtenirMoviments {

    @Autowired NamedParameterJdbcTemplate jdbcAmes;
    @Autowired FitxaRepository obtenirFitxa;
    @Autowired IObtenirClientAds obtenirClientAds;
    @Autowired ObjectMapper jsonMapper;
    Json json;

    @PostConstruct
    public void init() {
        json = new Json(jsonMapper);
    }

    public ObtenirMovimentsResponse executar(ObtenirMovimentsRequest request) {
        var keyFitxa = KeyFitxa.of(request.articleClient(), request.empresa(), request.magatzem());
        long stockActual = obtenirFitxa.find(keyFitxa).map(f -> f.stock()).orElse(0L);

        var params = new MapSqlParameterSource("artint", request.articleClient().artint())
                .addValue("clicod", request.articleClient().clicod())
                .addValue("empresa", request.empresa())
                .addValue("magatzem", request.magatzem())
                .addValue("dataInici", request.dataInici());

        var sql = """
                SELECT
                    m.id,
                    m.data,
                    m.tipus,
                    m.quantitat,
                    m.linia_albara_numero,
                    a.client as sortida_client,
                    m.traspas_client_receptor,
                    m.traspas_magatzem_receptor,
                    m.traspas_empresa_receptora,
                    cc.nom                     AS sortida_client_nom,
                    m.entrada_id_entrada_fabrica AS entrada_fabrica,
                    la.preu                    AS preu,
                    la.divisa                  AS divisa,
                    la.identificador_consum    AS identificador_consum,
                    a.numero_albara_especial   AS albara_especial,
                    a.tipus                    AS tipus_albara,
                    m.observacions,
                    COALESCE((a.informacio_magatzem->>'isEnPreparacio')::boolean, false) AS is_en_preparacio,
                    COALESCE((a.informacio_magatzem->>'isServit')::boolean, false)     AS is_servit,
                    COALESCE((a.informacio_magatzem->>'isEntregat')::boolean, false)   AS is_entregat,
                    COALESCE(a.is_facturat, false)                                     AS is_facturat,
                    a.informacio_enviament
                FROM inventari.moviment m
                LEFT JOIN albarans.linia_albara la ON
                    la.empresa     = m.linia_albara_empresa AND
                    la.codi_albara = m.linia_albara_numero  AND
                    la.linia       = m.linia_albara_linia
                LEFT JOIN albarans.albara a ON
                    a.empresa = m.linia_albara_empresa AND
                    a.codi    = m.linia_albara_numero
                LEFT JOIN (
                    SELECT DISTINCT ON (clicod) clicod, nom
                    FROM cache.cache_client
                ) cc ON cc.clicod = a.client
                WHERE m.artint    = :artint
                  AND m.clicod    = :clicod
                  AND m.empresa   = :empresa
                  AND m.magatzem  = :magatzem
                  AND m.data     >= :dataInici
                ORDER BY m.data DESC, m.id DESC
                """;

        var optClient = obtenirClientAds.get(request.articleClient().clicod());
        List<ItemMoviment> resultat = jdbcAmes.query(sql, params, new ItemMovimentExtractor(stockActual, request.dataFi(), optClient.orElse(null), json));

        if (request.isOrdreAscendent())
            Collections.reverse(resultat);

        long existenciaInicial;
        if (resultat.isEmpty()) {
            existenciaInicial = stockActual;
        } else {
            // L'existència del primer moviment és la d'abans de desfer-lo: per tenir la inicial
            // cal desfer també aquest (mateix criteri que ItemMovimentExtractor).
            var first = resultat.get(0);
            existenciaInicial = first.existencia() - first.tipus().quantitatCalculFitxa(first.quantitat());
        }

        // Filter by type AFTER existenciaInicial calculation to preserve correct running balances
        List<ItemMoviment> filtrat = request.filtresMoviment().isEmpty() ? resultat :
                resultat.stream()
                        .filter(m -> request.filtresMoviment().stream().anyMatch(f -> matchFiltre(m, f)))
                        .toList();

        return ObtenirMovimentsResponseImpl.builder()
                .moviments(filtrat)
                .existenciaInicial(existenciaInicial)
                .build();
    }

    public enum FiltreMoviment {
        ENTRADA, DEVOLUCIO_FABRICA, SORTIDA, DEVOLUCIO_CLIENT,
        FERRALLA, TRASPAS_CLIENT, TRASPAS_MAGATZEM, TRASPAS_EMPRESA,
        REGULARITZACIO, COMPRA_EXISTENCIES,
    }

    @JsonDeserialize(builder = ObtenirMovimentsRequestImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirMovimentsRequest {
        KeyArticleClient articleClient();
        String empresa();
        String magatzem();
        LocalDate dataInici();
        LocalDate dataFi();
        boolean isOrdreAscendent();
        @Value.Default
        default List<FiltreMoviment> filtresMoviment() { return List.of(); }
    }

    private static boolean matchFiltre(ItemMoviment m, FiltreMoviment filtre) {
        return switch (filtre) {
            case ENTRADA            -> TipusMoviment.ENTRADA.equals(m.tipus()) && m.quantitat() > 0;
            case DEVOLUCIO_FABRICA  -> TipusMoviment.ENTRADA.equals(m.tipus()) && m.quantitat() < 0;
            case SORTIDA            -> TipusMoviment.SORTIDA.equals(m.tipus()) && m.quantitat() > 0;
            case DEVOLUCIO_CLIENT   -> TipusMoviment.SORTIDA.equals(m.tipus()) && m.quantitat() < 0;
            case FERRALLA           -> TipusMoviment.FERRALLA.equals(m.tipus());
            case TRASPAS_CLIENT     -> TipusMoviment.TRASPAS_CLIENT.equals(m.tipus());
            case TRASPAS_MAGATZEM   -> TipusMoviment.TRASPAS_MAGATZEM.equals(m.tipus());
            case TRASPAS_EMPRESA    -> TipusMoviment.TRASPAS_EMPRESA.equals(m.tipus());
            case REGULARITZACIO     -> TipusMoviment.REGULARITZACIO.equals(m.tipus());
            case COMPRA_EXISTENCIES -> TipusMoviment.COMPRA_EXISTENCIES.equals(m.tipus());
        };
    }

    @JsonDeserialize(builder = ObtenirMovimentsResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirMovimentsResponse {
        List<ItemMoviment> moviments();
        Long existenciaInicial();

        @Derived
        default long existenciaFinal() {
            if (moviments().isEmpty()) return existenciaInicial();
            return moviments().get(moviments().size() - 1).existencia();
        }

        @Derived
        default long entrades() {
            return moviments().stream()
                    .filter(m -> TipusMoviment.ENTRADA.equals(m.tipus()) && m.quantitat() > 0)
                    .mapToLong(ItemMoviment::quantitat).sum();
        }

        @Derived
        default long devolucionsAFabrica() {
            return moviments().stream()
                    .filter(m -> TipusMoviment.ENTRADA.equals(m.tipus()) && m.quantitat() < 0)
                    .mapToLong(ItemMoviment::quantitat).sum();
        }

        @Derived
        default long sortides() {
            return moviments().stream()
                    .filter(m -> TipusMoviment.SORTIDA.equals(m.tipus()) && m.quantitat() > 0)
                    .mapToLong(ItemMoviment::quantitat).sum();
        }

        @Derived
        default long devolucionsDeClient() {
            return moviments().stream()
                    .filter(m -> TipusMoviment.SORTIDA.equals(m.tipus()) && m.quantitat() < 0)
                    .mapToLong(ItemMoviment::quantitat).sum();
        }

        @Derived
        default long ferralla() {
            return moviments().stream()
                    .filter(m -> TipusMoviment.FERRALLA.equals(m.tipus()))
                    .mapToLong(ItemMoviment::quantitat).sum();
        }

        @Derived
        default long regularitzacio() {
            return moviments().stream()
                    .filter(m -> TipusMoviment.REGULARITZACIO.equals(m.tipus()))
                    .mapToLong(ItemMoviment::quantitat).sum();
        }

        @Derived
        default long traspasMagatzem() {
            return moviments().stream()
                    .filter(m -> TipusMoviment.TRASPAS_MAGATZEM.equals(m.tipus()))
                    .mapToLong(ItemMoviment::quantitat).sum();
        }

        @Derived
        default long traspasClient() {
            return moviments().stream()
                    .filter(m -> TipusMoviment.TRASPAS_CLIENT.equals(m.tipus()))
                    .mapToLong(ItemMoviment::quantitat).sum();
        }

        @Derived
        default long traspasEmpresa() {
            return moviments().stream()
                    .filter(m -> TipusMoviment.TRASPAS_EMPRESA.equals(m.tipus()))
                    .mapToLong(ItemMoviment::quantitat).sum();
        }

        @Derived
        default long compraExistencies() {
            return moviments().stream()
                    .filter(m -> TipusMoviment.COMPRA_EXISTENCIES.equals(m.tipus()))
                    .mapToLong(ItemMoviment::quantitat).sum();
        }
    }

    @JsonDeserialize(builder = ItemMovimentImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ItemMoviment {
        Long id();
        LocalDate data();
        TipusMoviment tipus();
        Long quantitat();
        Long existencia();
        Optional<Long>       numeroAlbara();
        Optional<String>     sortidaClientCodi();
        Optional<String>     sortidaClientNom();
        Optional<String>     entradaFabrica();
        Optional<String>     traspasClientReceptor();
        Optional<String>     traspasMagatzemReceptor();
        Optional<String>     traspasEmpresaReceptora();
        Optional<BigDecimal> preu();
        Optional<Divisa>     divisa();
        Optional<String>     identificadorConsum();
        Optional<String>     albaraEspecial();
        Optional<TipusAlbara> tipusAlbara();
        Optional<String>     observacions();
        boolean isEnPreparacio();
        boolean isServit();
        boolean isEntregat();
        boolean isFacturat();
        Optional<FormaEnviament> formaEnviament();
        Optional<Incoterm>       incoterm();
        Optional<String>         desti();
        Optional<String>         transportista();
        boolean                  isMateixaFormaEnviamentHabitual();
    }

}

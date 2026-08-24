package ames.comercial.propostes.internal.application.query;

import ames.comercial.comandes.ext.IObtenirAlbaransServitsPerLinia;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.comandes.response.AlbaraServitLiniaComanda;
import ames.comercial.inventari.ext.IObtenirFitxesMagatzem;
import ames.comercial.inventari.ext.IObtenirFitxesMagatzem.ObtenirFitxesMagatzemResponse;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Retorna, sota demanda, les línies de comanda que originen un grup de traspàs
 * (article-client + magatzem origen/destí dins d'un rang de dates). Serveix per
 * mostrar la "raó del traspàs" amb el mateix detall que la vista de comandes especials
 * (quantitats, servida, acumulat, preu, dates, adjunts i comentaris), ja que la proposta
 * de traspàs agrupa aquestes línies i no en conserva el detall.
 */
@Component
public class ObtenirLiniesTraspasGrup {

    @Autowired NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired IObtenirAlbaransServitsPerLinia obtenirAlbaransServitsPerLinia;
    @Autowired IObtenirFitxesMagatzem obtenirFitxesMagatzem;

    public LiniesTraspasGrupResponse executar(String artint, String clicod, String client, String empresa,
                                              String magOrigen, String magDesti,
                                              LocalDate dataPrevistaInicial, LocalDate dataPrevistaFinal) {
        var params = new MapSqlParameterSource()
                .addValue("artint", artint)
                .addValue("clicod", clicod)
                .addValue("client", client)
                .addValue("empresa", empresa)
                .addValue("magOrigen", magOrigen)
                .addValue("magDesti", magDesti)
                .addValue("dataPrevistaInicial", dataPrevistaInicial)
                .addValue("dataPrevistaFinal", dataPrevistaFinal)
                .addValue("tipusArticleMedical", TipusArticleClient.MEDICAL.name());
        String sql = """
                SELECT lc.comanda, lc.numero, lc.tipus AS tipusLinia, com.empresa AS empresa,
                       lc.data_solicitada AS dataSolicitada,
                       lc.data_prevista_sortida AS dataSortida,
                       lc.data_prevista_sortida_interna AS dataSortidaInterna,
                       lc.data_confirmada_fabrica AS dataConfirmada,
                       lc.quantitat AS quantitat,
                       lc.quantitat_servida AS qtatServida,
                       lc.quantitat_pendent AS qtatPendent,
                       lc.preu AS preu, lc.divisa AS divisa, lc.preu_fixat AS preuFixat,
                       lc.comanda_blanca AS comandaBlanca,
                       com.informacio_client ->> 'identificador' AS comandaClient,
                       COALESCE(lcc.intern, '') AS comentarisInterns,
                       COALESCE(lcc.client, '') AS comentarisClient
                FROM comandes.linia_comanda lc
                JOIN comandes.comanda com ON lc.comanda = com.codi
                LEFT JOIN comandes.linia_comanda_comentaris lcc ON lcc.comanda = lc.comanda AND lcc.numero = lc.numero
                JOIN cache.cache_article_client ac ON lc.artint = ac.artint AND lc.clicod = ac.clicod
                WHERE lc.actual
                  AND NOT lc.servida
                  AND lc.tipus <> 'INVENT'
                  AND lc.artint = :artint
                  AND lc.clicod = :clicod
                  AND com.client = :client
                  AND com.empresa = :empresa
                  AND ac.mag_entrada_codi = :magOrigen
                  AND ac.mag_sortida_codi = :magDesti
                  AND lc.data_prevista_sortida_interna >= :dataPrevistaInicial
                  AND lc.data_prevista_sortida_interna <= :dataPrevistaFinal
                  -- Mateixa exclusió de Medical que a la proposta de traspàs, perquè els acumulats
                  -- del detall quadrin amb els del grup (l'empresa ja queda coberta per com.empresa = :empresa)
                  AND lc.tipus_article_client <> :tipusArticleMedical
                ORDER BY lc.data_prevista_sortida_interna ASC, lc.comanda ASC, lc.numero ASC;
                """;
        // Albarans que han servit cada línia (una sola consulta per a tot l'article-client, igual que a comandes especials)
        var albaransPerLinia = obtenirAlbaransServitsPerLinia.executar(KeyArticleClient.of(artint, clicod));

        // S'usa un ResultSetExtractor per poder calcular l'acumulat (suma corrent de la quantitat pendent
        // en l'ordre del resultat) amb una variable local normal, igual que a comandes especials.
        ResultSetExtractor<List<LiniaTraspasGrup>> extractor = rs -> {
            List<LiniaTraspasGrup> resultat = new ArrayList<>();
            long acumulat = 0;
            while (rs.next()) {
                acumulat += rs.getLong("qtatPendent");
                long comanda = rs.getLong("comanda");
                long numero = rs.getLong("numero");
                resultat.add(LiniaTraspasGrupImpl.builder()
                        .comanda(comanda)
                        .numero(numero)
                        .albarans(albaransPerLinia.getOrDefault(KeyLiniaComanda.of(comanda, numero), List.of()))
                        .empresa(rs.getString("empresa"))
                        .tipusLinia(TipusLiniaComanda.valueOf(rs.getString("tipusLinia")))
                        .comandaClient(MapperUtils.readOptionalString(rs, "comandaClient"))
                        .dataSolicitada(MapperUtils.readOptionalDate(rs, "dataSolicitada"))
                        .dataSortida(MapperUtils.readOptionalDate(rs, "dataSortida"))
                        .dataSortidaInterna(MapperUtils.readOptionalDate(rs, "dataSortidaInterna"))
                        .dataConfirmada(MapperUtils.readOptionalDate(rs, "dataConfirmada"))
                        .quantitat(rs.getLong("quantitat"))
                        .quantitatServida(rs.getLong("qtatServida"))
                        .qtatPendent(rs.getInt("qtatPendent"))
                        .quantitatAcumulada(acumulat)
                        .preu(Preu.of(rs.getBigDecimal("preu"), rs.getString("divisa")))
                        .isPreuFixat(rs.getBoolean("preuFixat"))
                        .comandaBlanca(MapperUtils.readOptionalLong(rs, "comandaBlanca"))
                        .comentarisInterns(rs.getString("comentarisInterns"))
                        .comentarisClient(rs.getString("comentarisClient"))
                        .build());
            }
            return resultat;
        };
        var linies = jdbcTemplate.query(sql, params, extractor);

        // Fitxa d'stock (la mateixa que mostra l'ajuda) i stock disponible al destí per marcar el tall.
        var fitxaStock = obtenirFitxesMagatzem.executar(KeyArticleClient.of(artint, clicod));
        long stockDesti = fitxaStock.fitxes().stream()
                .filter(f -> f.empresa().equals(empresa) && f.magatzem().equals(magDesti))
                .map(f -> f.stockTotal())
                .findFirst()
                .orElse(0L);

        return LiniesTraspasGrupResponseImpl.builder()
                .linies(marcarPrimeraLiniaNecessitaStock(linies, stockDesti))
                .fitxaStock(fitxaStock)
                .build();
    }

    // Marca la primera línia on l'acumulat supera l'stock del destí: és la primera que el traspàs
    // ha de cobrir (l'stock del destí cobreix les línies més antigues, per ordre de data).
    private List<LiniaTraspasGrup> marcarPrimeraLiniaNecessitaStock(List<LiniaTraspasGrup> linies, long stockDesti) {
        List<LiniaTraspasGrup> resultat = new ArrayList<>(linies.size());
        boolean marcada = false;
        for (var linia : linies) {
            if (!marcada && linia.quantitatAcumulada() > stockDesti) {
                resultat.add(LiniaTraspasGrupImpl.builder().from(linia).isPrimeraLiniaNecessitaStock(true).build());
                marcada = true;
            } else {
                resultat.add(linia);
            }
        }
        return resultat;
    }

    @JsonDeserialize(builder = LiniaTraspasGrupImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface LiniaTraspasGrup {
        long comanda();
        long numero();
        String empresa();
        List<AlbaraServitLiniaComanda> albarans();
        TipusLiniaComanda tipusLinia();
        Optional<String> comandaClient();
        Optional<LocalDate> dataSolicitada();
        Optional<LocalDate> dataSortida();
        Optional<LocalDate> dataSortidaInterna();
        Optional<LocalDate> dataConfirmada();
        long quantitat();
        long quantitatServida();
        int qtatPendent();
        long quantitatAcumulada();
        Preu preu();
        boolean isPreuFixat();
        Optional<Long> comandaBlanca();
        String comentarisInterns();
        String comentarisClient();

        // Cert per a la primera línia que el traspàs ha de cobrir (primera amb acumulat > stock destí).
        @Default default boolean isPrimeraLiniaNecessitaStock() { return false; }

        @Derived
        default String codiNumeroFormat() {
            return String.format("%07d", comanda()) + " / " + String.format("%04d", numero());
        }
    }

    // Resposta única del detall d'un grup de traspàs: les línies (amb la marca de tall) i la fitxa
    // d'stock. Tot es calcula al servidor perquè el frontend només hagi de pintar la resposta.
    @JsonDeserialize(builder = LiniesTraspasGrupResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface LiniesTraspasGrupResponse {
        List<LiniaTraspasGrup> linies();
        ObtenirFitxesMagatzemResponse fitxaStock();
    }

}

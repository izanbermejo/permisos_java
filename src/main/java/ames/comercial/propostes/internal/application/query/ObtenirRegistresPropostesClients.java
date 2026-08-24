package ames.comercial.propostes.internal.application.query;

import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Numbers;
import ames.comercial.shared.Preu;
import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static ames.comercial.shared.Numbers.decimal;
import static ames.comercial.shared.Numbers.descompteAplicar;

@Component
public class ObtenirRegistresPropostesClients {

    @Autowired NamedParameterJdbcTemplate jdbcTemplate;

    public List<RegArticlesPropostesClient> executar(String client, String empresa, String magatzem, LocalDate dataPrevistaInicial, LocalDate dataPrevistaFinal) {
        return executar(client, empresa, magatzem, dataPrevistaInicial, dataPrevistaFinal, List.of(), "");
    }

    public List<RegArticlesPropostesClient> executar(String magatzem, LocalDate dataPrevistaInicial,
                                                     LocalDate dataPrevistaFinal, List<String> responsable,
                                                     String transportista) {
        return executar("", "", magatzem, dataPrevistaInicial, dataPrevistaFinal, responsable, transportista);
    }

    public List<RegArticlesPropostesClient> executar(String client, String empresa, String magatzem, LocalDate dataPrevistaInicial,
                                                                            LocalDate dataPrevistaFinal, List<String> responsable,
                                                                            String transportista) {
        var params = new MapSqlParameterSource();
        params.addValue("client", client)
                .addValue("empresa", empresa)
                .addValue("magatzem", magatzem)
                .addValue("dataPrevistaInicial", dataPrevistaInicial)
                .addValue("dataPrevistaFinal", dataPrevistaFinal)
                .addValue("responsable", responsable.toArray(new String[0]), Types.ARRAY)
                .addValue("transportista", transportista)
                .addValue("empresaMedical", Empresa.MEDICAL.clau())
                .addValue("tipusArticleMedical", TipusArticleClient.MEDICAL.name());
        String sql = """
                      SELECT
                                  com.client, com.empresa as empresa_entrega, cli.nom as clientNom,
                                  com.import_net, com.divisa as divisaComanda,
                                  lc.artint, lc.clicod, lc.tipus_article_client,
                                  lc.comanda, lc.numero, lc.tipus as tipusLinia,
                                  com.informacio_client ->> 'identificador' as comandaClient,
                                  lc.data_solicitada, lc.data_prevista_sortida, data_confirmada_fabrica,
                                  lcc.intern <> '' AS teComentarisInterns,
                                  lcc.client <> '' AS teComentarisClient,
                                  COALESCE(lcc.intern, '') AS comentarisInterns,
                                  COALESCE(lcc.client, '') AS comentarisClient,
                                  ac.codi_fabrica, ac.referencia,
                                  ac.unitats_embalatge, ac.caixes_palet, ac.pes,
                                  ac.fabrica_codi, ac.fabrica_desc,
                                  ac.notes_embalatge is not null as teNotesEmbalatge,
                                  COALESCE(ac.notes_embalatge, '') AS notesEmbalatge,
                                  cli.data_bloqueig IS NOT NULL as clientBloquejat, ac.bloquejat as articleclientBloquejat,
                                  ac.partida_arant_codi, ac.partida_arant_desc, ac.planificador,
                                  lc.preu_fixat, lc.preu, lc.divisa, COALESCE((dades_calcul ->> 'descompte')::numeric, 0) AS descompte,
                                  lc.quantitat, lc.quantitat_pendent, lc.quantitat_servida, lc.quantitat_reservada,
                                  lc.comanda_blanca
                              FROM comandes.linia_comanda lc
                              JOIN comandes.comanda com ON lc.comanda = com.codi
                              LEFT JOIN comandes.linia_comanda_comentaris lcc ON lcc.comanda = lc.comanda AND lcc.numero = lc.numero
                              JOIN cache.cache_client cli ON com.client = cli.clicod
                              JOIN cache.cache_article_client ac ON lc.artint = ac.artint AND lc.clicod = ac.clicod AND ac.mag_sortida_codi = :magatzem
                              WHERE
                                 -- Línies actuals pendents de servir amb data de sortida prevista dins del rang
                                 lc.actual
                                 AND NOT lc.servida
                                 AND lc.data_prevista_sortida BETWEEN :dataPrevistaInicial AND :dataPrevistaFinal
                                 -- Client
                                 AND (
                                     (:client IS NULL OR :client = '' OR cli.clicod = :client)
                                     AND (
                                         :responsable IS NULL
                                         OR cardinality(:responsable) = 0
                                         OR cli.responsable = ANY(:responsable)
                                     )
                                 )
                                 -- Empresa
                                 AND (
                                     :empresa IS NULL
                                     OR :empresa = ''
                                     OR com.empresa = :empresa
                                 )
                                 -- Transportista
                                 AND (
                                     :transportista IS NULL
                                     OR :transportista = ''
                                     OR com.informacio_enviament ->> 'transportista' = :transportista
                                 )
                                 -- Medical queda fora de les propostes d'albarà: ni les comandes de
                                 -- l'empresa Medical ni les línies d'articles de tipologia Medical
                                 AND com.empresa <> :empresaMedical
                                 AND lc.tipus_article_client <> :tipusArticleMedical
                              ORDER BY com.client ASC, ac.codi_fabrica ASC, lc.data_prevista_sortida ASC;
        """;
        return jdbcTemplate.query(sql, params, (rs, rowNum) ->
                RegArticlesPropostesClientImpl.builder()
                        .client(rs.getString("client"))
                        .clientNom(rs.getString("clientNom"))
                        .empresaEntrega(rs.getString("empresa_entrega"))
                        .comanda(rs.getLong("comanda"))
                        .numero(rs.getLong("numero"))
                        .tipusLinia(TipusLiniaComanda.valueOf(rs.getString("tipusLinia")))
                        .comandaClient(rs.getString("comandaClient"))
                        .importNetComanda(MapperUtils.readOptionalBigdecimal(rs, "import_net"))
                        .divisaComanda(MapperUtils.readOptionalString(rs, "divisaComanda"))
                        .dataSolicitada(rs.getDate("data_solicitada").toLocalDate())
                        .dataSortida(rs.getDate("data_prevista_sortida").toLocalDate())
                        .dataConfirmadaFabrica(MapperUtils.readOptionalDate(rs, "data_confirmada_fabrica"))
                        .articleClient(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
                        .article(rs.getString("codi_fabrica"))
                        .referencia(rs.getString("referencia"))
                        .tipusArticleClient(TipusArticleClient.valueOf(rs.getString("tipus_article_client")))
                        .unitatsEmbalatge(rs.getInt("unitats_embalatge"))
                        .caixesPalet(rs.getInt("caixes_palet"))
                        .pesUnitari(rs.getBigDecimal("pes"))
                        .fabricaCodi(rs.getString("fabrica_codi"))
                        .fabricaDesc(rs.getString("fabrica_desc"))
                        .planificador(rs.getString("planificador"))
                        .isTeComentarisClient(rs.getBoolean("teComentarisClient"))
                        .isTeComentarisInterns(rs.getBoolean("teComentarisInterns"))
                        .isTeNotesEmbalatge(rs.getBoolean("teNotesEmbalatge"))
                        .comentarisInterns(rs.getString("comentarisInterns"))
                        .comentarisClient(rs.getString("comentarisClient"))
                        .notesEmbalatge(rs.getString("notesEmbalatge"))
                        .isClientBloquejat(rs.getBoolean("clientBloquejat"))
                        .isArticleclientBloquejat(rs.getBoolean("articleclientBloquejat"))
                        .partArantCodi(rs.getString("partida_arant_codi"))
                        .partArtantDesc(rs.getString("partida_arant_desc"))
                        .comandaBlanca(MapperUtils.readOptionalLong(rs, "comanda_blanca"))
                        .isPreuFixat(rs.getBoolean("preu_fixat"))
                        .preu(Preu.of(rs.getBigDecimal("preu"), Divisa.getBySymbol(rs.getString("divisa"))))
                        .descompte(rs.getBigDecimal("descompte"))
                        .qtatSolicitada(rs.getInt("quantitat"))
                        .qtatPendent(rs.getInt("quantitat_pendent"))
                        .qtatServida(rs.getInt("quantitat_servida"))
                        .qtatReservada(rs.getInt("quantitat_reservada"))
                        .stockServir(0)
                        .stockSatelit(0)
                        .build());
    }

    @JsonDeserialize(builder = RegArticlesPropostesClientImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface RegArticlesPropostesClient {
        String client();
        String clientNom();
        String empresaEntrega();
        Long comanda();
        Long numero();
        TipusLiniaComanda tipusLinia();
        String comandaClient();
        Optional<BigDecimal> importNetComanda();
        Optional<String> divisaComanda();
        LocalDate dataSolicitada();
        LocalDate dataSortida();
        Optional<LocalDate> dataConfirmadaFabrica();
        KeyArticleClient articleClient();
        @Derived default String artint() { return articleClient().artint(); }
        @Derived default String clicod() { return articleClient().clicod(); }
        String article();
        String referencia();
        TipusArticleClient tipusArticleClient();
        int unitatsEmbalatge();
        int caixesPalet();
        @Default
        default int unitatsPalet() { return unitatsEmbalatge() * caixesPalet(); }
        BigDecimal pesUnitari();
        String fabricaCodi();
        String fabricaDesc();
        String planificador();
        boolean isTeComentarisInterns();
        boolean isTeComentarisClient();
        boolean isTeNotesEmbalatge();
        @Default default String comentarisInterns() { return ""; }
        @Default default String comentarisClient() { return ""; }
        @Default default String notesEmbalatge() { return ""; }
        boolean isClientBloquejat();
        boolean isArticleclientBloquejat();
        String partArantCodi();
        String partArtantDesc();
        boolean isPreuFixat();
        Preu preu();
        BigDecimal descompte();
        Optional<Long> comandaBlanca();
        int qtatSolicitada();
        int qtatServida();
        int qtatPendent();
        int qtatReservada();
        int stockServir();
        int stockSatelit();

        @Default default boolean necessitaStockSatelit() { return false; }

        @Default default boolean seleccionat() { return false; }

        /**
         * Article-client normalitzat (clicod {@code 000000}): no està assignat a un client concret i
         * sempre juga al sistema de reserves. La quantitat a servir va lligada a la reserva, de manera
         * que no és editable i és sempre la quantitat reservada. Un article de tipus reserva (p.ex. filtre)
         * però venut a un client concret (clicod ≠ 000000) NO entra en aquesta casuística.
         */
        @Derived
        default boolean isNormalitzat() {
            return articleClient().isNormalitzat();
        }

        @Derived
        default int stockAlbara() {
            return isNormalitzat() ? qtatReservada() : stockServir();
        }

        @Derived
        default String codiNumeroFormat() {
            return String.format("%07d", comanda()) + " / " + String.format("%04d", numero());
        }

        /** Import net d'una quantitat: preu * quantitat aplicant el descompte (veure {@link Preu#impNet}). */
        static BigDecimal calcularImportNet(Preu preu, BigDecimal descompte, long quantitat) {
            return preu.impNet(quantitat, descompte);
        }

        /** Pes (Kg) d'una quantitat a partir del pes unitari (g). Únic punt de càlcul. */
        static BigDecimal calcularPes(BigDecimal pesUnitari, long quantitat) {
            return pesUnitari
                    .multiply(decimal(quantitat))
                    .divide(decimal(1_000), 2, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        @Derived default BigDecimal importNetPendent() { return calcularImportNet(preu(), descompte(), qtatPendent()); }

        @Derived default BigDecimal importNetAlbara() { return calcularImportNet(preu(), descompte(), stockAlbara()); }

        @Derived default BigDecimal pesPendent() { return calcularPes(pesUnitari(), qtatPendent()); }

        @Derived default BigDecimal pesAlbara() { return calcularPes(pesUnitari(), stockAlbara()); }

        @Derived default boolean isComandaNormalitzatSotaMinim() {
            // Les comandes especials no tenen l'import net calculat
            if (importNetComanda().isEmpty()  || divisaComanda().isEmpty())
                return false;
            // Per a estar sota del mínim la comanda ha de ser menor que 70EUR
            return Numbers.isHigh("70").than(importNetComanda().get()) && Divisa.EURO.symbol().equals(divisaComanda().get());
        }
    }

}

package ames.comercial.propostes.internal.application.query;

import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesClients.RegArticlesPropostesClient;
import ames.comercial.propostes.internal.application.query.ObtenirRegistresPropostesTraspas.CalculPropostesTraspasResponse.RegArticlesTraspasPendents;
import ames.comercial.propostes.internal.service.ConstruirResumMagatzems;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.Numbers;
import ames.comercial.shared.Preu;
import ames.comercial.shared.TipusArticleClient;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class ObtenirRegistresPropostesTraspas {

    @Autowired NamedParameterJdbcTemplate jdbcTemplate;

    // CTEs compartits entre la consulta agregada (SUM per grup) i la consulta de breakpoints
    // (una fila per línia amb l'acumulat). Es defineixen un sol cop per garantir que totes dues
    // apliquen EXACTAMENT els mateixos filtres i, per tant, l'acumulat quadra amb la qtat sol·licitada.
    private static final String CTES = """
            WITH filtered_lc AS (
              SELECT * FROM comandes.linia_comanda
              WHERE actual
                AND NOT servida
                AND NOT tipus = 'INVENT'
                AND data_prevista_sortida_interna >= :dataPrevistaInicial
                AND data_prevista_sortida_interna <= :dataPrevistaFinal
                -- Medical queda fora de les propostes d'albarà (la part d'empresa és a FILTRE_COMANDA)
                AND tipus_article_client <> :tipusArticleMedical
            ),
            filtered_clients AS (
              SELECT * FROM cache.cache_client
              WHERE (:responsable is null or cardinality(:responsable)=0)
                                OR (responsable = any(:responsable))
            ),
            filtered_ac AS (
              SELECT * FROM cache.cache_article_client
              WHERE mag_entrada_codi <> mag_sortida_codi
                AND mag_entrada_codi = :magatzemEntrada
                AND (:magatzemSortida IS NULL OR :magatzemSortida = '' OR mag_sortida_codi = :magatzemSortida)
            )
            """;

    // Filtres sobre la capçalera de la comanda. Es defineixen un sol cop pel mateix comentaris que els CTEs:
    // les dues consultes han d'aplicar exactament els mateixos filtres.
    private static final String FILTRE_COMANDA = """
            WHERE
                (:transportista IS NULL OR :transportista = '' OR com.informacio_enviament ->> 'transportista' = :transportista)
                -- Les comandes de l'empresa Medical no entren a les propostes d'albarà
                AND com.empresa <> :empresaMedical
            """;

    private static final String SQL_REGISTRES = CTES + """
            SELECT com.client, com.empresa as empresa_entrega, ac.empresa as empresa_origen, cli.nom as clientNom,
                lc.artint, lc.clicod,
                COALESCE(BOOL_OR(lcc.intern <> ''), FALSE) AS teComentarisInterns,
                COALESCE(BOOL_OR(lcc.client <> ''), FALSE) AS teComentarisClient,
                ac.codi_fabrica, ac.referencia,
                ac.unitats_embalatge, ac.caixes_palet, ac.pes,
                ac.fabrica_codi, ac.fabrica_desc,
                ac.mag_entrada_codi, ac.mag_entrada_desc, ac.notes_embalatge is not null as teNotesEmbalatge,
                COALESCE(ac.notes_embalatge, '') AS notesEmbalatge,
                ac.bloquejat, ac.partida_arant_codi, ac.partida_arant_desc, ac.planificador,
                ac.preu, ac.divisa, ac.preu_ames, ac.divisa_ames,
                ac.mag_sortida_codi, ac.mag_sortida_desc, SUM(lc.quantitat_pendent) as qtat
            FROM filtered_lc lc
            JOIN comandes.comanda com ON lc.comanda = com.codi
            LEFT JOIN comandes.linia_comanda_comentaris lcc ON lcc.comanda = lc.comanda AND lcc.numero = lc.numero
            JOIN filtered_clients cli ON com.client = cli.clicod
            JOIN filtered_ac ac ON lc.artint = ac.artint AND lc.clicod = ac.clicod
            """ + FILTRE_COMANDA + """
            GROUP BY com.client, com.empresa, ac.empresa, cli.nom, lc.artint, lc.clicod, ac.codi_fabrica, ac.referencia,
                ac.unitats_embalatge, ac.caixes_palet, ac.pes,
                ac.fabrica_codi, ac.fabrica_desc, ac.notes_embalatge,
                ac.bloquejat, ac.partida_arant_codi, ac.partida_arant_desc, ac.planificador,
                ac.preu, ac.divisa, ac.preu_ames, ac.divisa_ames,
                ac.mag_entrada_codi, ac.mag_entrada_desc, ac.mag_sortida_codi, ac.mag_sortida_desc;
            """;

    // Retorna les línies (sense agregar) ordenades per grup i per data, per poder acumular la quantitat
    // pendent en Java (mateix patró que ObtenirLiniesTraspasGrup) i trobar la primera línia del traspàs.
    // No es fa el JOIN de comentaris a propòsit: no cal aquí i evita duplicar files (i inflar l'acumulat).
    private static final String SQL_BREAKPOINTS = CTES + """
            SELECT com.client, com.empresa as empresa_entrega, ac.empresa as empresa_origen,
                lc.artint, lc.clicod, lc.comanda, ac.mag_entrada_codi, ac.mag_sortida_codi,
                com.informacio_client ->> 'identificador' AS comanda_client,
                com.informacio_client ->> 'programa' AS programa,
                lc.data_solicitada, lc.data_prevista_sortida, lc.data_prevista_sortida_interna,
                lc.quantitat_pendent
            FROM filtered_lc lc
            JOIN comandes.comanda com ON lc.comanda = com.codi
            JOIN filtered_clients cli ON com.client = cli.clicod
            JOIN filtered_ac ac ON lc.artint = ac.artint AND lc.clicod = ac.clicod
            """ + FILTRE_COMANDA + """
            ORDER BY com.client, com.empresa, ac.empresa, lc.artint, lc.clicod,
                ac.mag_entrada_codi, ac.mag_sortida_codi,
                lc.data_prevista_sortida_interna, lc.comanda, lc.numero;
            """;

    public List<RegArticlesTraspasPendents> executar(String magatzemEntrada, String magatzemSortida, LocalDate dataPrevistaInicial,
                                                     LocalDate dataPrevistaFinal) {
        return executar(magatzemEntrada, magatzemSortida, dataPrevistaInicial, dataPrevistaFinal, List.of(), "");
    }

    public List<RegArticlesTraspasPendents> executar(String magatzemEntrada, LocalDate dataPrevistaInicial,
                                                     LocalDate dataPrevistaFinal, List<String> responsable, String transportista) {
        return executar(magatzemEntrada, "", dataPrevistaInicial, dataPrevistaFinal, responsable, transportista);
    }

    public List<RegArticlesTraspasPendents> executar(String magatzemEntrada, String magatzemSortida, LocalDate dataPrevistaInicial,
                                                     LocalDate dataPrevistaFinal, List<String> responsable, String transportista) {
        var params = new MapSqlParameterSource();
        params.addValue("magatzemEntrada", magatzemEntrada)
                .addValue("magatzemSortida", magatzemSortida)
                .addValue("dataPrevistaInicial", dataPrevistaInicial)
                .addValue("dataPrevistaFinal", dataPrevistaFinal)
                .addValue("responsable", responsable.toArray(new String[0]), Types.ARRAY)
                .addValue("transportista", transportista)
                .addValue("empresaMedical", Empresa.MEDICAL.clau())
                .addValue("tipusArticleMedical", TipusArticleClient.MEDICAL.name());

        // Breakpoints per grup (acumulat + les 3 dates de cada línia, ordenades per data). Serveixen
        // perquè CalcularPropostesTraspas, un cop coneix l'stock del destí, trobi la primera línia
        // que el traspàs ha de cobrir.
        var breakpointsPerGrup = obtenirBreakpoints(params);

        return jdbcTemplate.query(SQL_REGISTRES, params, (rs, rowNum) ->
                RegArticlesTraspasPendentsImpl.builder()
                        .client(rs.getString("client"))
                        .clientNom(rs.getString("clientNom"))
                        .empresaEntrega(rs.getString("empresa_entrega"))
                        .empresaOrigen(rs.getString("empresa_origen"))
                        .artint(rs.getString("artint"))
                        .clicod(rs.getString("clicod"))
                        .article(rs.getString("codi_fabrica"))
                        .referencia(rs.getString("referencia"))
                        .unitatsEmbalatge(rs.getInt("unitats_embalatge"))
                        .caixesPalet(rs.getInt("caixes_palet"))
                        .pesUnitari(rs.getBigDecimal("pes"))
                        .magOrigen(rs.getString("mag_entrada_codi"))
                        .magOrigenDesc(rs.getString("mag_entrada_desc"))
                        .magEntrega(rs.getString("mag_sortida_codi"))
                        .magEntregaDesc(rs.getString("mag_sortida_desc"))
                        .fabricaCodi(rs.getString("fabrica_codi"))
                        .fabricaDesc(rs.getString("fabrica_desc"))
                        .planificador(rs.getString("planificador"))
                        .isTeComentarisClient(rs.getBoolean("teComentarisClient"))
                        .isTeComentarisInterns(rs.getBoolean("teComentarisInterns"))
                        .isTeNotesEmbalatge(rs.getBoolean("teNotesEmbalatge"))
                        .notesEmbalatge(rs.getString("notesEmbalatge"))
                        .isBloquejat(rs.getBoolean("bloquejat"))
                        .partArantCodi(rs.getString("partida_arant_codi"))
                        .partArtantDesc(rs.getString("partida_arant_desc"))
                        .preuClient(llegirPreu(rs, "preu", "divisa"))
                        .preuAmes(llegirPreu(rs, "preu_ames", "divisa_ames"))
                        .qtatSolicitada(rs.getInt("qtat"))
                        .stockOrigen(0)
                        .stockOrigenSatelit(0)
                        .stockDesti(0)
                        .qtatTraspas(0)
                        .qtatTraspassable(0)
                        .breakpoints(breakpointsPerGrup.getOrDefault(keyDe(rs), List.of()))
                        .build());
    }

    private Map<KeyGrupTraspas, List<LiniaBreakpointTraspas>> obtenirBreakpoints(MapSqlParameterSource params) {
        ResultSetExtractor<Map<KeyGrupTraspas, List<LiniaBreakpointTraspas>>> extractor = rs -> {
            Map<KeyGrupTraspas, List<LiniaBreakpointTraspas>> resultat = new HashMap<>();
            Map<KeyGrupTraspas, Long> acumPerGrup = new HashMap<>();
            while (rs.next()) {
                var key = keyDe(rs);
                long acum = acumPerGrup.merge(key, rs.getLong("quantitat_pendent"), Long::sum);
                resultat.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new LiniaBreakpointTraspas(
                                acum,
                                MapperUtils.readOptionalDate(rs, "data_solicitada"),
                                MapperUtils.readOptionalDate(rs, "data_prevista_sortida"),
                                MapperUtils.readOptionalDate(rs, "data_prevista_sortida_interna"),
                                rs.getLong("comanda"),
                                Optional.ofNullable(rs.getString("comanda_client")).orElse(""),
                                Optional.ofNullable(rs.getString("programa")).orElse("")));
            }
            return resultat;
        };
        return jdbcTemplate.query(SQL_BREAKPOINTS, params, extractor);
    }

    /**
     * Llegeix del cache d'articles-client un parell (preu, divisa) com a tarifa. Es considera que la tarifa
     * no està informada quan el preu és nul o zero, el mateix criteri amb què el cache desa el preu AMES i
     * el cost. Si el preu hi és però la divisa ve buida s'agafa l'euro: val més valorar amb la divisa per
     * defecte que fer caure tota la proposta per un article-client amb la divisa sense informar.
     */
    private Optional<Preu> llegirPreu(ResultSet rs, String columnaPreu, String columnaDivisa) throws SQLException {
        var valor = rs.getBigDecimal(columnaPreu);
        if (valor == null || valor.compareTo(BigDecimal.ZERO) == 0)
            return Optional.empty();
        return Optional.of(Preu.of(valor, MapperUtils.readOptionalString(rs, columnaDivisa).orElse(Divisa.EURO.symbol())));
    }

    private KeyGrupTraspas keyDe(ResultSet rs) throws SQLException {
        return new KeyGrupTraspas(
                rs.getString("client"), rs.getString("empresa_entrega"), rs.getString("empresa_origen"),
                rs.getString("artint"), rs.getString("clicod"),
                rs.getString("mag_entrada_codi"), rs.getString("mag_sortida_codi"));
    }

    // Identitat d'un grup de traspàs, coincident amb les claus del GROUP BY de la consulta agregada.
    public record KeyGrupTraspas(String client, String empresaEntrega, String empresaOrigen,
                                 String artint, String clicod, String magOrigen, String magEntrega) {}

    // Una línia amb la quantitat acumulada del grup fins a ella (ordre per data), les seves 3 dates
    // i la informació de comanda (comanda, comanda segons client i programa) per identificar-la.
    public record LiniaBreakpointTraspas(long acum, Optional<LocalDate> dataSolicitada,
                                         Optional<LocalDate> dataSortida, Optional<LocalDate> dataSortidaInterna,
                                         long comanda, String comandaClient, String programa) {}

    @JsonDeserialize(builder = CalculPropostesTraspasResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface CalculPropostesTraspasResponse {

        List<RegArticlesTraspasPendents> traspassos();

        @Derived
        default List<ResumMagatzem> resumMagatzem() {
            return new ConstruirResumMagatzems().build(traspassos());
        }

        // Les 3 dates de la primera línia que el traspàs ha de cobrir (un cop l'stock del destí ha
        // absorbit, per ordre de data, les línies més antigues).
        @JsonDeserialize(builder = DatesPrimeraLiniaTraspasImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface DatesPrimeraLiniaTraspas {
            Optional<LocalDate> dataSolicitada();
            Optional<LocalDate> dataSortida();
            Optional<LocalDate> dataSortidaInterna();
        }

        // Informació de comanda de la primera línia que el traspàs ha de cobrir. La reenvia el frontend
        // en crear un traspàs a plataforma perquè cada línia d'albarà en porti la referència d'origen.
        @JsonDeserialize(builder = InfoComandaPrimeraLiniaTraspasImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface InfoComandaPrimeraLiniaTraspas {
            long comanda();
            String comandaClient();
            String programa();
        }

        @JsonDeserialize(builder = RegArticlesTraspasPendentsImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface RegArticlesTraspasPendents {
            String client();
            String clientNom();
            String empresaOrigen();
            String empresaEntrega();
            String artint();
            String clicod();
            String article();
            String referencia();
            int unitatsEmbalatge();
            int caixesPalet();
            @Default default int unitatsPalet() { return unitatsEmbalatge() * caixesPalet(); }
            BigDecimal pesUnitari();
            String magOrigen();
            String magOrigenDesc();
            String magEntrega();
            String magEntregaDesc();
            String fabricaCodi();
            String fabricaDesc();
            String planificador();
            boolean isTeComentarisInterns();
            boolean isTeComentarisClient();
            boolean isTeNotesEmbalatge();
            @Default default String notesEmbalatge() { return ""; }
            boolean isBloquejat();
            String partArantCodi();
            String partArtantDesc();

            /** Tarifa de client de la peça (Advantage {@code ARTCLI.ACLPRE}); buida si no està informada */
            Optional<Preu> preuClient();

            /** Tarifa AMES de traspàs interempresa (Advantage {@code ARTCLI.PREAMES}); buida si no està informada */
            Optional<Preu> preuAmes();

            /**
             * Cert si el traspàs es facturarà i, per tant, la línia d'albarà es valorarà amb la tarifa AMES.
             * Mateix criteri que {@code Albara.isTraspasFacturable()}: hi ha canvi d'empresa. L'abonable no
             * hi entra perquè creant des de la proposta sempre és fals.
             * <p>
             * Els traspassos a plataforma no necessiten cap comprovació addicional: un magatzem de plataforma
             * no té mai canvi d'empresa, de manera que ja cauen aquí com a no facturables. Si algun dia una
             * peça amb empresa d'entrega diferent s'entregués a una plataforma, aquest camp diria que és
             * facturable mentre que {@code CrearAlbaraTraspasPlataforma} valoraria la línia amb la tarifa de
             * client (força l'empresa de destí a la d'origen), i caldria descartar aquí els destins plataforma.
             */
            @Derived default boolean isTraspasFacturable() { return !empresaOrigen().equals(empresaEntrega()); }

            int stockOrigen();
            int stockOrigenSatelit();
            int stockDesti();
            int qtatSolicitada();
            int qtatTraspas();
            int qtatTraspassable();
            @Derived default int qtatTraspassableDefinitiva() { return qtatTraspassable(); }
            @Default default boolean seleccionat() { return false; }

            // Dates de la primera línia del traspàs (les calcula CalcularPropostesTraspas amb l'stock del destí).
            Optional<DatesPrimeraLiniaTraspas> datesPrimeraLiniaTraspas();

            // Informació de comanda de la primera línia del traspàs (mateixa línia que datesPrimeraLiniaTraspas).
            Optional<InfoComandaPrimeraLiniaTraspas> infoComandaPrimeraLinia();

            // Auxiliar: acumulats + dates de cada línia del grup. Només s'usa durant el càlcul,
            // no forma part de la identitat del registre ni es serialitza cap al frontend.
            @JsonIgnore
            @Value.Auxiliary
            @Default default List<LiniaBreakpointTraspas> breakpoints() { return List.of(); }

            @Derived default int stockOrigenNoSatelit() { return Math.max(0, stockOrigen() - stockOrigenSatelit()); }

            @Derived default boolean necessitaStockSatelit() { return stockOrigenNoSatelit() < qtatTraspassableDefinitiva(); }

            @Derived default String artintClicod() { return artint() + clicod(); }

            @Derived default BigDecimal pesTraspas() { return pesUnitari()
                    .multiply(Numbers.decimal(qtatTraspas())
                            .divide(Numbers.decimal(1_000), 2, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP); }

            @Derived default BigDecimal pesTraspassable() { return pesUnitari()
                    .multiply(Numbers.decimal(qtatTraspassable())
                            .divide(Numbers.decimal(1_000), 2, RoundingMode.HALF_UP))
                    .setScale(2, RoundingMode.HALF_UP); }

            /**
             * Preu amb què es valorarà la línia d'albarà del traspàs: la tarifa AMES si es factura i la de
             * client si no (mateix criteri que {@code CrearAlbaraTraspas} amb {@code PreusProviderSelector}).
             * Si la tarifa que toca no està informada, el preu és zero — igual que farà la creació de
             * l'albarà — amb la divisa de l'altra tarifa, o l'euro si no n'hi ha cap.
             */
            @Derived default Preu preuTraspas() {
                var tarifa = isTraspasFacturable() ? preuAmes() : preuClient();
                return tarifa.orElseGet(() -> Preu.of(BigDecimal.ZERO, divisaAlternativa()));
            }

            /**
             * Cert si la tarifa que valorarà el traspàs no està informada i, per tant, la línia d'albarà
             * naixerà a preu zero. En un traspàs facturable això vol dir una línia pendent de facturar
             * valorada a zero, que és el cas que interessa detectar abans de crear l'albarà.
             */
            @Derived default boolean isSenseTarifa() {
                return isTraspasFacturable() ? preuAmes().isEmpty() : preuClient().isEmpty();
            }

            /**
             * Import del traspàs. Es calcula amb el mateix mètode que les propostes de client (sense
             * descompte, que als traspassos no s'aplica) perquè el que mostra el grid i el que retorna el
             * recàlcul en editar la quantitat no puguin divergir.
             */
            @Derived default BigDecimal importTraspas() { return calcularImport(preuTraspas(), qtatTraspas()); }

            @Derived default BigDecimal importTraspassable() { return calcularImport(preuTraspas(), qtatTraspassable()); }

            static BigDecimal calcularImport(Preu preu, long quantitat) {
                return RegArticlesPropostesClient.calcularImportNet(preu, BigDecimal.ZERO, quantitat);
            }

            /** Divisa de la tarifa que no s'aplica, per valorar a zero amb una divisa coherent amb la peça */
            private Divisa divisaAlternativa() {
                return (isTraspasFacturable() ? preuClient() : preuAmes())
                        .map(Preu::divisa)
                        .orElse(Divisa.EURO);
            }
        }

        @JsonDeserialize(builder = ResumMagatzemImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface ResumMagatzem {
            String magatzem();
            String magatzemDesc();
            long numTraspassos();
            long numTraspassosAmbStockCap();
            long numTraspassosAmbStockParcial();
            long numTraspassosAmbStockTot();
            boolean isHiHaPecesSatelit();
            boolean isNecessitaPecesDeSatelit();
            // Les 3 dates de la propera necessitat d'stock del magatzem: l'article amb la
            // dataSortidaInterna més propera entre els seus traspassos (quan s'esgota l'stock del destí).
            Optional<DatesPrimeraLiniaTraspas> datesProperaNecessitatStock();
        }

    }

}

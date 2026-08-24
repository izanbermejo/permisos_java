package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import ames.comercial.albarans.internal.domain.linia.InformacioPesa;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.ws.rs.QueryParam;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BuscarAlbarans {

    /** Cap albarà té codi negatiu: força que la cerca no retorni res */
    private static final String CAP_NUMERO = "-1";

    @Autowired
    NamedParameterJdbcTemplate jdbc;

    public List<BuscarAlbaransResponse> executar(BuscarAlbaransRequest req) {
        String sql = """
                SELECT
                    a.codi, a.empresa, a.tipus,
                    a.client, c.nom AS clientNom,
                    a.data, a.magatzem,
                    a.observacions_impressio, a.observacions_internes,
                    a.is_tancat, a.is_facturat, a.is_facturacio_automatica, a.numero_proveidor, a.numero_albara_especial,
                    (a.informacio_traspas ->> 'isTraspasAbonable')::boolean AS is_traspas_abonable,
                    l.linia, l.artint, l.clicod, l.comanda, l.comanda_client,
                    l.programa, l.pesa_matriu, l.pesa_referencia, l.pesa_nivell_tecnic, l.pesa_denominacio,
                    l.pesa_pes_unitari, l.pesa_unitats_embalatge, l.pesa_bosses_caixa, l.pesa_caixes_palet,
                    l.quantitat, l.quantitat_pendent_facturar,
                    l.preu, l.divisa, l.descompte, l.identificador_consum,
                    l.comanda_blanca, l.observacions_impressio AS linia_observacions_impressio,
                    l.observacions_internes AS linia_observacions_internes,
                    l.usuari, l.datareg_local,
                    count(adj.albara) as num_adjunts, l.is_urgent as liniaAlbaraUrgent, a.is_urgent as capsaleraAlbaraUrgent
                FROM (
                    SELECT *
                    FROM albarans.albara
                    WHERE empresa = :empresa
                      AND (:numeros IS NULL OR codi = ANY (string_to_array(:numeros, ',')::bigint[]))
                      AND (:client    IS NULL OR :client    = '' OR client             = :client)
                      AND (:magatzemSortida IS NULL OR :magatzemSortida = '' OR magatzem = :magatzemSortida)
                      AND (:magatzemRecepcio IS NULL OR :magatzemRecepcio = '' OR
                           (informacio_traspas IS NOT NULL AND informacio_traspas ->> 'magatzemReceptor' = :magatzemRecepcio))
                      AND (:transportista IS NULL OR :transportista = '' OR informacio_enviament ->> 'transportista' = :transportista)
                      AND (:refTransport IS NULL OR :refTransport = '' OR referencia_transport = :refTransport)
                      AND (:albEspecial  IS NULL OR :albEspecial  = '' OR numero_albara_especial = :albEspecial)
                      AND (:dataInici IS NULL OR data >= :dataInici)
                      AND (:dataFi   IS NULL OR data <= :dataFi)
                      AND (:servida  IS NULL OR COALESCE((informacio_magatzem ->> 'isServit')::boolean, false) = :servida)
                      AND (:articleClient IS NULL OR :articleClient = '' OR EXISTS (
                          SELECT 1 FROM albarans.linia_albara l2
                          WHERE l2.codi_albara = codi AND l2.empresa = empresa AND l2.artint = :articleClient
                      ))
                      AND (:comandaClient IS NULL OR :comandaClient = '' OR EXISTS (
                          SELECT 1 FROM albarans.linia_albara l2
                          WHERE l2.codi_albara = codi AND l2.empresa = empresa AND l2.comanda_client = :comandaClient
                      ))
                      AND (:usuarisCreacio IS NULL OR
                           COALESCE(id_usuari_creacio, 0) = ANY (string_to_array(:usuarisCreacio, ',')::bigint[]))
                      AND (:tipus IS NULL OR :tipus = '' OR tipus = :tipus)
                      AND (:facturat IS NULL OR is_facturat = :facturat)
                      AND (:tancat IS NULL OR is_tancat = :tancat)
                      AND (:identificadorConsum IS NULL OR :identificadorConsum = '' OR EXISTS (
                          SELECT 1 FROM albarans.linia_albara l2
                          WHERE l2.codi_albara = codi AND l2.empresa = empresa
                            AND l2.identificador_consum ILIKE '%' || :identificadorConsum || '%'
                      ))
                      AND (:pendentFacturar IS NULL OR EXISTS (
                          SELECT 1 FROM albarans.linia_albara l2
                          WHERE l2.codi_albara = codi AND l2.empresa = empresa AND l2.quantitat_pendent_facturar > 0
                      ) = :pendentFacturar)
                    ORDER BY codi DESC
                    LIMIT 100
                ) a
                LEFT JOIN albarans.linia_albara l
                    ON a.codi = l.codi_albara AND a.empresa = l.empresa
                LEFT JOIN cache.cache_client c
                    ON a.client = c.clicod
                LEFT JOIN albarans.adjunt adj
                    ON adj.albara = a.codi
                GROUP BY a.codi, a.empresa, a.tipus,
                    a.client, c.nom,
                    a.data, a.magatzem,
                    a.observacions_impressio, a.observacions_internes,
                    a.is_tancat, a.is_facturat, a.is_facturacio_automatica, a.numero_proveidor, a.numero_albara_especial,
                    a.informacio_traspas,
                    a.is_tancat, a.is_facturat, a.numero_proveidor, a.numero_albara_especial,
                    l.linia, l.artint, l.clicod, l.comanda, l.comanda_client,
                    l.programa, l.pesa_matriu, l.pesa_referencia, l.pesa_nivell_tecnic, l.pesa_denominacio,
                    l.pesa_pes_unitari, l.pesa_unitats_embalatge, l.pesa_bosses_caixa, l.pesa_caixes_palet,
                    l.quantitat, l.quantitat_pendent_facturar,
                    l.preu, l.divisa, l.descompte, l.identificador_consum,
                    l.comanda_blanca, l.observacions_impressio,
                    l.observacions_internes,
                    l.usuari, l.datareg_local, l.is_urgent, a.is_urgent
                ORDER BY a.codi DESC, l.linia;
                """;

        var params = new MapSqlParameterSource();
        params.addValue("empresa",         req.empresa,         Types.VARCHAR);
        params.addValue("numeros",         normalitzarNumeros(req.numeros), Types.VARCHAR);
        params.addValue("client",          req.client,          Types.VARCHAR);
        params.addValue("articleClient",   req.articleClient,   Types.VARCHAR);
        params.addValue("comandaClient",   req.comandaClient,   Types.VARCHAR);
        params.addValue("magatzemSortida", req.magatzemSortida, Types.VARCHAR);
        params.addValue("magatzemRecepcio",req.magatzemRecepcio,Types.VARCHAR);
        params.addValue("transportista",   req.transportista,   Types.VARCHAR);
        params.addValue("refTransport",    req.refTransport,    Types.VARCHAR);
        params.addValue("albEspecial",     req.albEspecial,     Types.VARCHAR);
        params.addValue("dataInici",       req.dataInici != null ? LocalDate.parse(req.dataInici) : null, Types.DATE);
        params.addValue("dataFi",          req.dataFi   != null ? LocalDate.parse(req.dataFi)   : null, Types.DATE);
        params.addValue("servida",         req.servida,         Types.BOOLEAN);
        params.addValue("tipus",           req.tipus,           Types.VARCHAR);
        params.addValue("facturat",        req.facturat,        Types.BOOLEAN);
        params.addValue("tancat",          req.tancat,          Types.BOOLEAN);
        params.addValue("usuarisCreacio",  normalitzarNumeros(req.usuarisCreacio), Types.VARCHAR);
        params.addValue("identificadorConsum", req.identificadorConsum, Types.VARCHAR);
        params.addValue("pendentFacturar", req.pendentFacturar, Types.BOOLEAN);
        return jdbc.query(sql, params, this::extractData);
    }

    private List<BuscarAlbaransResponse> extractData(ResultSet rs) throws SQLException {
        Map<KeyAlbara, BuscarAlbaransResponse> map = new LinkedHashMap<>();
        while (rs.next()) {
            var keyAlbara = KeyAlbara.of(rs.getLong("codi"), rs.getString("empresa"));
            var albara = map.get(keyAlbara);

            if (albara == null) {
                albara = BuscarAlbaransResponseImpl.builder()
                        .id(keyAlbara)
                        .tipus(TipusAlbara.valueOf(rs.getString("tipus")))
                        .client(Optional.ofNullable(rs.getString("client")))
                        .clientNom(Optional.ofNullable(rs.getString("clientNom")))
                        .data(rs.getDate("data").toLocalDate())
                        .magatzem(rs.getString("magatzem"))
                        .observacionsImpressio(Optional.ofNullable(rs.getString("observacions_impressio")))
                        .observacionsInternes(Optional.ofNullable(rs.getString("observacions_internes")))
                        .isTancat(rs.getBoolean("is_tancat"))
                        .isFacturat(rs.getBoolean("is_facturat"))
                        .isFacturacioAutomatica(rs.getBoolean("is_facturacio_automatica"))
                        .isTraspasAbonable(rs.getBoolean("is_traspas_abonable"))
                        .numeroAlbaraEspecial(MapperUtils.readOptionalString(rs, "numero_albara_especial"))
                        .numAdjunts(rs.getLong("num_adjunts"))
                        .isUrgent(rs.getBoolean("capsaleraAlbaraUrgent"))
                        .linies(new ArrayList<>())
                        .build();
                map.put(keyAlbara, albara);
            }

            var numLinia = rs.getLong("linia");
            if (!rs.wasNull()) {
                albara = BuscarAlbaransResponseImpl.builder()
                        .from(albara)
                        .addLinies(BuscarAlbaransLiniaResponseImpl.builder()
                                .id(KeyLiniaAlbara.of(keyAlbara, numLinia))
                                .articleClient(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
                                .comanda(MapperUtils.readOptionalLong(rs, "comanda"))
                                .comandaClient(MapperUtils.readOptionalString(rs, "comanda_client"))
                                .programaClient(MapperUtils.readOptionalString(rs, "programa"))
                                .pesaMatriu(rs.getString("pesa_matriu"))
                                .pesaReferencia(rs.getString("pesa_referencia"))
                                .pesaNivellTecnic(rs.getString("pesa_nivell_tecnic"))
                                .pesaDenominacio(rs.getString("pesa_denominacio"))
                                // Les línies migrades poden no tenir-los informats, per això es tracta el NULL
                                .pesaPesUnitari(Optional.ofNullable(rs.getBigDecimal("pesa_pes_unitari")).orElse(BigDecimal.ZERO))
                                .pesaUnitatsEmbalatge(rs.getLong("pesa_unitats_embalatge"))
                                .pesaBossesCaixa(rs.getLong("pesa_bosses_caixa"))
                                .pesaCaixesPalet(rs.getLong("pesa_caixes_palet"))
                                .quantitat(rs.getLong("quantitat"))
                                .quantitatPendentFacturar(rs.getLong("quantitat_pendent_facturar"))
                                .identificadorConsum(MapperUtils.readOptionalString(rs, "identificador_consum"))
                                .preu(Preu.of(rs.getBigDecimal("preu"), Divisa.getBySymbol(rs.getString("divisa"))))
                                // Les línies anteriors a la columna (i les de traspàs i consum) no en tenen: queden a 0
                                .descompte(Optional.ofNullable(rs.getBigDecimal("descompte")).orElse(BigDecimal.ZERO))
                                .comandaBlanca(MapperUtils.readOptionalLong(rs, "comanda_blanca"))
                                .observacionsImpressio(MapperUtils.readOptionalString(rs, "linia_observacions_impressio"))
                                .observacionsInternes(MapperUtils.readOptionalString(rs, "linia_observacions_internes"))
                                .usuari(rs.getString("usuari"))
                                .data(rs.getDate("datareg_local").toLocalDate())
                                .isUrgent(rs.getBoolean("liniaAlbaraUrgent"))
                                .build())
                        .build();
                map.put(keyAlbara, albara);
            }
        }
        return new ArrayList<>(map.values());
    }

    /**
     * Normalitza una llista de números escrits per l'usuari ("0001234, 1235, abc") a la cadena canònica
     * que espera la SQL ("1234,1235"): parteix per comes, descarta el que no siguin dígits i treu els
     * zeros del davant, sense duplicats. S'utilitza tant per als números d'albarà com per als codis
     * dels usuaris de creació.
     * <p>
     * Retorna null si el filtre no s'ha informat (no filtra) i {@link #CAP_NUMERO} si s'ha informat però
     * cap valor és un número vàlid: l'usuari ha demanat filtrar per números i cap és utilitzable, de
     * manera que la cerca no ha de retornar res en comptes de tornar tots els albarans.
     */
    private String normalitzarNumeros(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        var numeros = Arrays.stream(valor.split(","))
                .map(String::trim)
                // Fins a 18 dígits: més no cap a un bigint i el cast de la SQL fallaria
                .filter(n -> n.matches("\\d{1,18}"))
                .map(n -> String.valueOf(Long.parseLong(n)))
                .distinct()
                .collect(Collectors.joining(","));
        return numeros.isEmpty() ? CAP_NUMERO : numeros;
    }

    private Long mapNullableLong(ResultSet rs, String column) throws SQLException {
        var value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    public static class BuscarAlbaransRequest {
        @QueryParam("empresa")          String  empresa;
        /** Números d'albarà separats per comes, tal com els escriu l'usuari (veure normalitzarNumeros) */
        @QueryParam("numeros")          String  numeros;
        @QueryParam("client")           String  client;
        @QueryParam("articleClient")    String  articleClient;
        @QueryParam("comandaClient")    String  comandaClient;
        @QueryParam("magatzemSortida")  String  magatzemSortida;
        @QueryParam("magatzemRecepcio") String  magatzemRecepcio;
        @QueryParam("transportista")    String  transportista;
        @QueryParam("refTransport")     String  refTransport;
        @QueryParam("albEspecial")      String  albEspecial;
        /**
         * Codis (usufab) dels usuaris que han creat l'albarà, separats per comes; buit vol dir tots.
         * El 0 són els albarans sense usuari de creació informat (veure ObtenirUsuarisCreadorsAlbarans).
         */
        @QueryParam("usuarisCreacio")   String  usuarisCreacio;
        @QueryParam("dataInici")        String  dataInici;
        @QueryParam("dataFi")           String  dataFi;
        @QueryParam("servida")          Boolean servida;
        @QueryParam("tipus")            String  tipus;
        @QueryParam("facturat")         Boolean facturat;
        @QueryParam("tancat")           Boolean tancat;
        @QueryParam("identificadorConsum") String identificadorConsum;
        @QueryParam("pendentFacturar")  Boolean pendentFacturar;
    }

    @JsonDeserialize(builder = BuscarAlbaransResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface BuscarAlbaransResponse {
        KeyAlbara id();
        TipusAlbara tipus();
        Optional<String> client();
        Optional<String> clientNom();
        LocalDate data();
        String magatzem();
        Optional<String> observacionsImpressio();
        Optional<String> observacionsInternes();
        boolean isTancat();
        boolean isFacturat();
        boolean isFacturacioAutomatica();
        /** Cert si és un traspàs abonable: les seves línies no es facturen mai (veure {@code penabo}) */
        boolean isTraspasAbonable();
        Optional<String> numeroAlbaraEspecial();
        List<BuscarAlbaransLiniaResponse> linies();
        long numAdjunts();
        boolean isUrgent();

        @Value.Derived
        default boolean isTeAdjunts(){
            return numAdjunts() > 0;
        }

        /**
         * Cert si l'albarà ja s'ha començat a facturar: està marcat com a facturat o té alguna línia
         * facturada. Mentre sigui fals es pot canviar el flag d'autofacturable.
         */
        @Value.Derived
        default boolean isFacturacioIniciada() {
            return isFacturat() || linies().stream().anyMatch(BuscarAlbaransLiniaResponse::isFacturacioIniciada);
        }

        /** Cert si l'albarà té alguna línia pendent de facturar (útil per als consums). */
        @Value.Derived
        default boolean teLiniaPendentFacturar() {
            return linies().stream().anyMatch(l -> l.quantitatPendentFacturar() > 0);
        }

        @JsonDeserialize(builder = BuscarAlbaransLiniaResponseImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface BuscarAlbaransLiniaResponse {
            KeyLiniaAlbara id();
            KeyArticleClient articleClient();
            Optional<Long> comanda();
            Optional<String> comandaClient();
            Optional<String> programaClient();
            String pesaMatriu();
            String pesaReferencia();
            String pesaNivellTecnic();
            String pesaDenominacio();
            /** Pes unitari de la peça en grams (Advantage {@code ART.ARTPFIN}) */
            BigDecimal pesaPesUnitari();
            /**
             * Unitats del nivell base d'embalatge (Advantage {@code ACLUCAI}); 0 si no n'hi ha.
             * Als no normalitzats són les peces per caixa; als normalitzats, les peces per bossa.
             */
            long pesaUnitatsEmbalatge();
            /** Bosses per caixa de l'article-client (Advantage {@code BOSXCAI}); 0 si no n'hi ha */
            long pesaBossesCaixa();
            /** Caixes per palet de l'article-client (Advantage {@code ACLUCAP}); 0 si no n'hi ha */
            long pesaCaixesPalet();
            long quantitat();
            long quantitatPendentFacturar();
            Optional<String> identificadorConsum();
            Preu preu();
            /**
             * Descompte comercial en % sobre el preu brut de la línia; 0 si no n'hi ha.
             * Veure {@link ames.comercial.albarans.internal.domain.linia.LiniaAlbara#descompte()}.
             */
            BigDecimal descompte();
            Optional<Long> comandaBlanca();
            Optional<String> observacionsImpressio();
            Optional<String> observacionsInternes();
            String usuari();
            LocalDate data();
            boolean isUrgent();

            /** Cert si la línia ja s'ha començat a facturar, encara que sigui parcialment. */
            @Value.Derived
            default boolean isFacturacioIniciada() {
                return quantitatPendentFacturar() < quantitat();
            }

            /** Peces per palet: el condicionament complet. 0 si no hi ha els dos factors informats. */
            @Value.Derived
            default long pesaUnitatsPalet() {
                return pesaUnitatsEmbalatge() * pesaCaixesPalet();
            }

            /** Pes total de la línia en Kg: pes unitari de la peça (g) × quantitat servida. */
            @Value.Derived
            default BigDecimal pesTotal() {
                return InformacioPesa.calcularPesKg(pesaPesUnitari(), quantitat());
            }

            /** Import brut de la línia: preu × quantitat, sense aplicar-hi el descompte. */
            @Value.Derived
            default BigDecimal importBrut() {
                return preu().impBrut(quantitat());
            }

            /** Import net de la línia: l'import brut amb el {@link #descompte()} aplicat. */
            @Value.Derived
            default BigDecimal importNet() {
                return preu().impNet(quantitat(), descompte());
            }

            /**
             * Divisa dels imports: la base del preu, que als articles tarifats per cèntims no és la
             * mateixa que la del preu ({@code EUR%} contra {@code EUR}).
             */
            @Value.Derived
            default Divisa divisaImport() {
                return preu().divisa().base();
            }
        }
    }

}

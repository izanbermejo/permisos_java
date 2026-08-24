package ames.comercial.albarans.internal.application.query;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.IObtenirInformacioAcumulatsArticleClient;
import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import ames.comercial.advantage.internal.ObtenirInformacioAcumulatsArticleClientAds.InformacioAcumulatsArticleClient;
import ames.comercial.albarans.internal.application.query.ObtenirUltimsAlbarans.ObtenirUltimsAlbaransResponse.ObtenirUltimsAlbaransResponseAlbarans;
import ames.comercial.albarans.internal.domain.albara.InformacioMagatzem;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.server.Json;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.FormaEnviament;
import ames.comercial.shared.Incoterm;
import ames.comercial.shared.InformacioEnviament;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;

import static java.lang.Integer.MAX_VALUE;

@Service
public class ObtenirUltimsAlbarans {

    @Autowired IObtenirInformacioAcumulatsArticleClient obtenirInformacioAcumulatsArticleClient;
    @Autowired IObtenirClientAds obtenirClientAds;
    @Autowired NamedParameterJdbcTemplate jdbcAmes;
    @Autowired ObjectMapper jsonMapper;
    Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    public ObtenirUltimsAlbaransResponse executar (KeyArticleClient articleClient,
                                                   String codiAlbaraFacturaRefereciaTransit,
                                                   long acumulatSegonsClient) {
        return executar(articleClient, codiAlbaraFacturaRefereciaTransit, acumulatSegonsClient, MAX_VALUE);
    }

    public ObtenirUltimsAlbaransResponse executar (KeyArticleClient articleClient,
                                                   String codiAlbaraFacturaRefereciaTransit,
                                                   long acumulatSegonsClient,
                                                   int limitAlbarans) {
        // Obtenció de l'informació d'acumulats per article client
        var optInformacioAcumulats = obtenirInformacioAcumulatsArticleClient.get(articleClient);

        // Obtenció del client per a la forma d'enviament habitual
        var client = obtenirClientAds.get(articleClient.clicod()).orElseThrow(() -> new ClientNoExisteix(articleClient.clicod()));

        // Obtenir totes les línies d'albarans associades agrupades i amb un ResultSetExtractor
        // anar acumulant segons l'informació d'acumulats
        var albarans = obtenirAlbarans(articleClient, optInformacioAcumulats, codiAlbaraFacturaRefereciaTransit, client);

        // Es reverteix la llista per a que els albarans apareguin ordenats de més recent a més antic
        var albaransRevertits = new ArrayList<>(albarans);
        Collections.reverse(albaransRevertits);

        // Només es retornen els X albarans més recents dins del limit establert
        var albaransLimitats = albaransRevertits.size() > limitAlbarans ? albaransRevertits.subList(0, limitAlbarans) : albaransRevertits;

        return ObtenirUltimsAlbaransResponseImpl.builder()
                .acumulatSegonsClient(acumulatSegonsClient)
                .quantitatAcumuladaDefinidaArticleclient(optInformacioAcumulats.quantitatAcumulada())
                .numAlbaraFacturaDefinidaArticleclient(optInformacioAcumulats.numAlbara())
                .dataAcumulatDefinidaArticleclient(optInformacioAcumulats.data())
                .albarans(albaransLimitats)
                .formaEnviamentHabitualClient(client.formaEnviament())
                .incotermHabitualClient(client.incoterm())
                .destiHabitualClient(client.desti())
                .build();
    }

    private List<ObtenirUltimsAlbaransResponseAlbarans> obtenirAlbarans(KeyArticleClient articleClient,
                                                                        InformacioAcumulatsArticleClient informacioAcumulats,
                                                                        String codiAlbaraFacturaRefereciaTransit,
                                                                        ClientAds client) {
        var params = new MapSqlParameterSource();
        params.addValue("artint", articleClient.artint());
        params.addValue("clicod", articleClient.clicod());
        var sql = """
                WITH linies AS (
                        SELECT
                            codi_albara,
                            empresa,
                            SUM(quantitat) AS quantitat
                        FROM albarans.linia_albara
                        WHERE artint = :artint
                            AND clicod = :clicod
                        GROUP BY codi_albara, empresa
                    )
                    SELECT
                        a.codi,
                        a.empresa,
                        a."data",
                        a.numero_albara_especial,
                        a.informacio_enviament,
                        a.informacio_magatzem,
                        n.nota,
                        l.quantitat,
                        STRING_AGG(DISTINCT f.codi_factura, ', ') AS factures
                    FROM linies l
                    LEFT JOIN albarans.albara a
                        ON l.codi_albara = a.codi AND l.empresa = a.empresa
                    LEFT JOIN albarans.facturacio f
                        ON f.codi_albara = a.codi AND f.empresa = a.empresa
                    LEFT JOIN albarans.nota_albara_pesa n
                        ON n.codi_albara = a.codi
                        AND n.empresa = a.empresa
                        AND n.artint = :artint
                        AND n.clicod = :clicod
                    WHERE 
                        (a.client = :clicod OR a.client IS NULL)    -- Els albarans de traspàs plataforma no tenen client associat
                      AND a.tipus IN ('CLIENT', 'TRASPAS_PLATAFORMA')
                    GROUP BY a.codi, a.empresa, a."data", n.nota, l.quantitat
                    ORDER BY a.codi ASC;
            """;
        return jdbcAmes.query(sql, params, rs -> {
            long acumulat = 0;
            long transit = 0;
            boolean isModeData = informacioAcumulats.data().isPresent();
            boolean acumulatIncialAplicat = false;
            boolean isReferenciaTransitTrobat = false;
            boolean isAlbaraReferenciaTransit = false;
            List<ObtenirUltimsAlbaransResponseAlbarans> resultat = new ArrayList<>();
            while (rs.next()) {
                var numAlbara = rs.getLong("codi");
                var optFactures = MapperUtils.readOptionalString(rs, "factures");
                var quantitatAlbara = rs.getLong("quantitat");
                var dataAlbara = rs.getDate("data").toLocalDate();
                var informacioEnviament = json.deserialize(rs.getString("informacio_enviament"), InformacioEnviament.class);
                var informacioMagatzem = json.deserialize(rs.getString("informacio_magatzem"), InformacioMagatzem.class);

                // En cas que no s'hagi trobat encara l'albarà de referència de trànsit, es comprova si l'albarà actual és l'albarà de referència de trànsit
                // i en cas que s'hagi trobat s'acumula al transit la quantitat de l'albarà
                isAlbaraReferenciaTransit = false;
                if (!isReferenciaTransitTrobat) {
                    isAlbaraReferenciaTransit = isAlbaraDeReferenciaTransit(numAlbara,
                            optFactures.orElse(""),
                            codiAlbaraFacturaRefereciaTransit == null ? "" : codiAlbaraFacturaRefereciaTransit.trim());
                    isReferenciaTransitTrobat = isAlbaraReferenciaTransit;
                } else {
                    transit += quantitatAlbara;
                }

                // Càlcul de la quantitat a acumular
                long quantitatAcumular = quantitatAcumular(numAlbara, quantitatAlbara, dataAlbara, informacioAcumulats);
                // Aplicar l'acumulat inicial només en el primer albarà que compleixi les condicions d'acumulat
                if (!acumulatIncialAplicat && quantitatAcumular > 0) {
                    acumulat = informacioAcumulats.quantitatAcumulada();
                    acumulatIncialAplicat = true;
                    // Quan no es mode data que es per número d'albarà el primer albarà no acumula la seva quantitat
                    if (!isModeData) {
                        quantitatAcumular = 0;
                    }
                }
                acumulat += quantitatAcumular;

                resultat.add(ObtenirUltimsAlbaransResponseAlbaransImpl.builder()
                        .id(KeyAlbara.of(numAlbara, rs.getString("empresa")))
                        .data(dataAlbara)
                        .isEntregat(informacioMagatzem.isEntregat())
                        .albaraEspecial(Optional.ofNullable(rs.getString("numero_albara_especial")))
                        .factures(optFactures)
                        .nota(Optional.ofNullable(rs.getString("nota")))
                        .quantitat(quantitatAlbara)
                        .formaEnviament(informacioEnviament.formaEnviament())
                        .incoterm(informacioEnviament.incoterm())
                        .desti(informacioEnviament.desti())
                        .transportista(informacioEnviament.transportista())
                        .acumulat(acumulat)
                        .transit(transit)
                        .isAlbaraDeReferenciaTransit(isAlbaraReferenciaTransit)
                        .isMateixaFormaEnviamentHabitual(isMateixaFormaEnviament(client, informacioEnviament))
                        .build());
            }
            return resultat;
        });
    }

    private long quantitatAcumular(long numAlbara, long quantitatAlbara, LocalDate dataAlbara, InformacioAcumulatsArticleClient informacioAcumulats) {
        // Si la data d'acumulats està informada només s'acumula en cas que la data de l'albarà sigui igual o posterior a la data d'acumulats
        if (informacioAcumulats.data().isPresent()) {
            var dataAcumulats = informacioAcumulats.data().get();
            return dataAlbara.isBefore(dataAcumulats) ? 0L : quantitatAlbara;
        }

        // Si el número d'albarà d'acumulats està informat només s'acumula en cas que el número d'albarà sigui igual o superior al número d'albarà d'acumulats
        if (informacioAcumulats.numAlbara().isPresent())
            return numAlbara >= informacioAcumulats.numAlbaraAsLong() ? quantitatAlbara : 0L;

        // En cas que no estigui informada ni la data ni el número d'albarà d'acumulats s'acumula tot
        return quantitatAlbara;
    }

    private boolean isAlbaraDeReferenciaTransit(long codiAlbara, String factures, String codiAlbaraFacturaReferenciaTransit) {
        return isMateixAlbara(codiAlbara, codiAlbaraFacturaReferenciaTransit) || isMateixaFactura(factures, codiAlbaraFacturaReferenciaTransit);
    }

    private boolean isMateixAlbara(long codiAlbara, String codiAlbaraFacturaReferenciaTransit) {
        try {
            return codiAlbara == Long.parseLong(codiAlbaraFacturaReferenciaTransit);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isMateixaFactura(String factures, String codiAlbaraFacturaReferenciaTransit) {
        if (factures == null || factures.isBlank()) {
            return false;
        }
        return Arrays.stream(factures.split(","))
                .map(String::trim)
                .anyMatch(codiAlbaraFacturaReferenciaTransit::equals);
    }

    private boolean isMateixaFormaEnviament(ClientAds client, InformacioEnviament informacioEnviament) {
        return client.formaEnviament().equals(informacioEnviament.formaEnviament()) &&
                client.incoterm().equals(informacioEnviament.incoterm()) &&
                client.desti().equals(informacioEnviament.desti());
    }

    @JsonDeserialize(builder = BuscarAlbaransResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirUltimsAlbaransResponse {
        long acumulatSegonsClient();
        // Informacio acumulats definida al sistema
        long quantitatAcumuladaDefinidaArticleclient();
        Optional<String> numAlbaraFacturaDefinidaArticleclient();
        Optional<LocalDate> dataAcumulatDefinidaArticleclient();
        // Llistat d'albarans
        List<ObtenirUltimsAlbaransResponseAlbarans> albarans();
        // Forma enviament habitual del client
        FormaEnviament formaEnviamentHabitualClient();
        Incoterm incotermHabitualClient();
        String destiHabitualClient();

        @Derived
        default boolean isAlbaraReferenciaTrobat() {
            return albarans().stream().anyMatch(ObtenirUltimsAlbaransResponseAlbarans::isAlbaraDeReferenciaTransit);
        }

        @Derived
        default long quantitatAcumuladaReferencia() {
            return albarans().stream().filter(ObtenirUltimsAlbaransResponseAlbarans::isAlbaraDeReferenciaTransit)
                    .findFirst()
                    .map(ObtenirUltimsAlbaransResponseAlbarans::acumulat)
                    .orElse(0L);
        }

        @Derived
        default boolean isAcumulatClientMateixReferencia() {
            return acumulatSegonsClient() == quantitatAcumuladaReferencia();
        }

        @Derived
        default long quantitatEnTransit() {
            return albarans().isEmpty() ? 0L : albarans().get(0).transit();
        }

        @Derived
        default long albaraEnTransit() {
            return albarans().stream()
                    .filter(ObtenirUltimsAlbaransResponseAlbarans::isAlbaraDeReferenciaTransit)
                    .findFirst()
                    .map(a -> a.id().codi())
                    .orElse(0L);
        }

        @JsonDeserialize(builder = ObtenirUltimsAlbaransResponseAlbaransImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface ObtenirUltimsAlbaransResponseAlbarans {
            KeyAlbara id();
            LocalDate data();
            boolean isEntregat();
            Optional<String> albaraEspecial();
            Optional<String> factures();
            Optional<String> nota();
            Long quantitat();
            FormaEnviament formaEnviament();
            Incoterm incoterm();
            String desti();
            Optional<String> transportista();
            Long acumulat();
            Long transit();
            boolean isAlbaraDeReferenciaTransit();
            boolean isMateixaFormaEnviamentHabitual();
        }
    }

}

package ames.comercial.comandes.internal.application.query;

import ames.comercial.comandes.ComandesException.ComandaNoExisteix;
import ames.comercial.comandes.internal.application.query.ObtenirComandaNormalitzat.ObtenirComandaNormalitzatResponse.DadesEnviamentJustificantResponse;
import ames.comercial.comandes.internal.application.query.ObtenirComandaNormalitzat.ObtenirComandaNormalitzatResponse.LiniaComandaResponse;
import ames.comercial.comandes.internal.domain.comanda.DadesEnviamentJustificant;
import ames.comercial.comandes.internal.domain.comanda.Servible;
import ames.comercial.comandes.internal.domain.linia.*;
import ames.comercial.comandes.internal.infraestructure.comanda.ComandaRepository;
import ames.comercial.comandes.response.InformacioEnviamentResponse;
import ames.comercial.comandes.service.IProviderStocks;
import ames.comercial.server.Json;
import ames.comercial.shared.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Default;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ames.comercial.shared.Numbers.decimal;
import static ames.comercial.shared.Numbers.descompteAplicar;

@Component
public class ObtenirComandaNormalitzat {

    @Autowired ComandaRepository comandaRepo;
    @Autowired IProviderStocks providerStocks;
    @Autowired ObjectMapper jsonMapper;
    @Autowired JdbcTemplate jdbc;
    private Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    public ObtenirComandaNormalitzatResponse executar (long id) {
        var comanda = comandaRepo.find(id).orElseThrow(ComandaNoExisteix::new);
        var dadesNormalitzat = comanda.dadesNormalitzat().orElseThrow();
        var tarifes = dadesNormalitzat.tarifes();
        return ObtenirComandaNormalitzatResponseImpl.builder()
                .comanda(comanda.codi())
                .codiClient(comanda.dades().client())
                .nomClient(comanda.dades().clientNom())
                .empresa(Empresa.getByClau(comanda.dades().empresa()))
                .servida(comanda.servida())
                .servible(comanda.servible())
                .tarifaCoixinets(tarifes.tarifaCoixinets())
                .tarifaBarres(tarifes.tarifaBarres())
                .tarifaMedical(tarifes.tarifaMedical())
                .tarifaIbinsa(tarifes.tarifaIbinsa())
                .tarifaFiltresBxx(tarifes.tarifaFiltresBxx())
                .tarifaFiltresSsu(tarifes.tarifaFiltresSsu())
                .tarifaFiltresSxx(tarifes.tarifaFiltresSxx())
                .tarifaFiltresSsuPlaques(tarifes.tarifaFiltresSsuPlaques())
                .comandaClient(comanda.informacioClient().identificador())
                .dataRecepcio(comanda.informacioClient().data())
                .importNet(dadesNormalitzat.importNet())
                .importBrut(dadesNormalitzat.importBrut())
                .divisa(dadesNormalitzat.divisa())
                .pes(dadesNormalitzat.pes())
                .costTransport(dadesNormalitzat.costTransport())
                .linies(buildLiniesComanda(id, comanda.dades().empresa()))
                .adresa(comanda.adresa())
                .informacioEnviament(InformacioEnviamentResponse.of(comanda.informacioEnviament()))
                .dadesEnviamentJustificant(DadesEnviamentJustificantResponse.of(comanda.dadesEnviamentJustificant()))
                .build();
    }

    private List<LiniaComandaResponse> buildLiniesComanda(long comanda, String empresa) {
        var listLiniesComanda = jdbc.query("""
                    SELECT lc.*, com.intern AS "comentarisIntern", com.client AS "comentarisClient", cac.codi_fabrica
                    FROM comandes.linia_comanda lc
                    LEFT JOIN comandes.linia_comanda_comentaris com ON lc.comanda = com.comanda AND lc.numero = com.numero
                    LEFT JOIN cache.cache_article_client cac ON lc.artint = cac.artint AND lc.clicod = cac.clicod
                    WHERE lc.comanda = ? AND actual = true
                        AND quantitat > 0
                    ORDER BY data_solicitada ASC, lc.numero;
                """,
                (rs, rowNum) -> {
                    var estatReserva = rs.getString("estat_reserva");
                    return LiniaComandaResponseImpl.builder()
                            .id(KeyLiniaComanda.of(rs.getLong("comanda"), rs.getLong("numero")))
                            .articleClient(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
                            .matriu(rs.getString("codi_fabrica"))
                            .referencia(rs.getString("referencia"))
                            .tipusArticleClient(TipusArticleClient.valueOf(rs.getString("tipus_article_client")))
                            .quantitat(rs.getLong("quantitat"))
                            .preu(Preu.of(rs.getBigDecimal("preu"), Divisa.getBySymbol(rs.getString("divisa"))))
                            .isPreuFixat(rs.getBoolean("preu_fixat"))
                            .dataSolicitada(rs.getDate("data_solicitada").toLocalDate())
                            .dataPrevistaSortida(rs.getDate("data_prevista_sortida").toLocalDate())
                            .quantitatServida(rs.getLong("quantitat_servida"))
                            .reserva(estatReserva == null
                                ? InformacioReserva.empty()
                                : InformacioReservaImpl.builder()
                                    .estat(Reservable.valueOf(rs.getString("estat_reserva")))
                                    .quantitat(rs.getLong("quantitat_reservada"))
                                    .build())
                            .dadesCalcul(readSafeDadesCalcul(rs))
                            .comentarisInterns(Optional.ofNullable(rs.getString("comentarisIntern")))
                            .comentarisClient(Optional.ofNullable(rs.getString("comentarisClient")))
                            .versio(rs.getString("versio"))
                            .build();
                },
                comanda);
        var listArticlesClient = listLiniesComanda.stream().map(LiniaComandaResponse::articleClient).collect(Collectors.toSet());
        var stocks = listArticlesClient.isEmpty()
                        ? Map.<KeyArticleClient, Stock>of()
                        : providerStocks.calculate(listArticlesClient, Empresa.getByClau(empresa));
        return listLiniesComanda.stream()
                .map(l -> (LiniaComandaResponse) LiniaComandaResponseImpl.builder()
                        .from(l)
                        .stock(stocks.getOrDefault(l.articleClient(), Stock.empty()).stock())
                        .build())
                .toList();
    }

    private Optional<DadesCalculNormalitzat> readSafeDadesCalcul(ResultSet rs) throws SQLException {
        var strDadesCalcul = rs.getString("dades_calcul");
        return strDadesCalcul == null ? Optional.empty() : Optional.of(json.deserialize(rs.getString("dades_calcul"), DadesCalculNormalitzat.class));
    }

    @JsonDeserialize(builder = ObtenirComandaNormalitzatResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirComandaNormalitzatResponse {
        long comanda();
        String codiClient();
        String nomClient();
        Empresa empresa();
        boolean servida();
        Servible servible();
        Optional<String> tarifaCoixinets();
        Optional<String> tarifaBarres();
        Optional<String> tarifaIbinsa();
        Optional<String> tarifaMedical();
        Optional<String> tarifaFiltresBxx();
        Optional<String> tarifaFiltresSsu();
        Optional<String> tarifaFiltresSxx();
        Optional<String> tarifaFiltresSsuPlaques();
        String comandaClient();
        LocalDate dataRecepcio();
        BigDecimal importNet();
        BigDecimal importBrut();
        Divisa divisa();
        BigDecimal pes();
        BigDecimal costTransport();
        List<LiniaComandaResponse> linies();
        Adresa adresa();
        InformacioEnviamentResponse informacioEnviament();
        Optional<DadesEnviamentJustificantResponse> dadesEnviamentJustificant();

        @Derived
        default boolean isConteLiniesPecesEspecials() {
            return linies().stream()
                    .filter(l -> l.quantitat() > 0)
                    .anyMatch(l -> l.tipusArticleClient() == TipusArticleClient.ESPECIAL);
        }

        @JsonDeserialize(builder = LiniaComandaResponseImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface LiniaComandaResponse {
            KeyLiniaComanda id();
            KeyArticleClient articleClient();
            String matriu();
            TipusArticleClient tipusArticleClient();
            String referencia();
            long quantitat();
            Preu preu();
            boolean isPreuFixat();
            LocalDate dataSolicitada();
            LocalDate dataPrevistaSortida();
            long quantitatServida();
            InformacioReserva reserva();
            Optional<DadesCalculNormalitzat> dadesCalcul();
            Optional<String> comentarisInterns();
            Optional<String> comentarisClient();
            String versio();
            @Default default long stock() { return 0L; }

            @Derived
            default long comanda() { return id().comanda(); }

            @Derived
            default long numero() { return id().numero(); }

            @Derived
            default BigDecimal importNet() {
                return preu().imp(quantitat())
                        .multiply(descompteAplicar(dadesCalcul().map(DadesCalculNormalitzat::descompte).orElse(BigDecimal.ZERO)))
                        .divide(decimal(100), 3, RoundingMode.HALF_UP);
            }

            @Derived
            default int setmana() {
                return dataSolicitada().get(WeekFields.ISO.weekOfWeekBasedYear());
            }

            @Derived
            default BigDecimal importBrut() { return preu().imp(quantitat());}

            @Derived
            default long quantitatPendent() { return Math.max(0, quantitat() - quantitatServida()); }

            @Derived
            default boolean servida() { return quantitatPendent() == 0; }

        }

        @JsonDeserialize(builder = DadesEnviamentJustificantResponseImpl.Builder.class)
        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface DadesEnviamentJustificantResponse {
            String to();
            String cc();
            String assumpte();
            String missatge();
            LocalDateTime data();
            String usuari();
            List<String> adjunts();

            static Optional<DadesEnviamentJustificantResponse> of(Optional<DadesEnviamentJustificant> optDades) {
                if (optDades.isEmpty())
                    return Optional.empty();
                var dades = optDades.get();
                return Optional.of(DadesEnviamentJustificantResponseImpl.builder()
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

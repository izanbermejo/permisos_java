package ames.comercial.comandes.internal.application.query;

import ames.comercial.advantage.IObtenirArticleClientInformacioComanda;
import ames.comercial.comandes.internal.domain.comanda.InformacioClient;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.server.Json;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.Preu;
import ames.comercial.shared.SharedExceptions.ArticleClientNotFound;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.immutables.value.Value.Derived;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirLiniesAnulades {

    @Autowired ObjectMapper jsonMapper;
    @Autowired NamedParameterJdbcTemplate jdbc;
    @Autowired IObtenirArticleClientInformacioComanda obtenirArticleClientInformacioComanda;
    Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    public List<ObtenirLiniesAnuladesResponse> executar (String articleClient, String comanda, LocalDate dataInici, LocalDate dataFi) {
        // Obtenció de l'article client (per obtenir l'artint)
        var info =  obtenirArticleClientInformacioComanda.executar(articleClient).orElseThrow(() -> new ArticleClientNotFound(articleClient));
        var params = new MapSqlParameterSource("artint", info.artint());
        params.addValue("clicod", info.codiClient());
        params.addValue("comanda", comanda);
        params.addValue("dataInici", dataInici);
        params.addValue("dataFi", dataFi);
        return jdbc.query("""
                SELECT l.*, c.*, com.intern as "comIntern", com.client as "comClient"
                FROM comandes.linia_comanda l
                LEFT JOIN comandes.comanda c ON c.codi = l.comanda
                LEFT JOIN comandes.linia_comanda_comentaris com ON l.comanda = com.comanda AND l.numero = com.numero
                WHERE
                    actual
                    AND quantitat = 0
                    AND artint = :artint
                    AND clicod = :clicod
                    -- Per identificador de comanda
                    AND (
                        (:comanda is null or length(:comanda) = 0)
                        or
                        (c.informacio_client ->> 'identificador'::varchar ilike '%' || :comanda || '%')
                    )
                    -- Per data
                    AND data_solicitada BETWEEN :dataInici AND :dataFi
                ORDER BY data_solicitada, l.comanda ASC, l.numero ASC
                LIMIT 100
                """, params, (rs, rowNum) -> {
                    InformacioClient infoClient = json.deserialize(rs.getString("informacio_client"), InformacioClient.class);
                    return ObtenirLiniesAnuladesResponseImpl.builder()
                            .codi(rs.getLong("comanda"))
                            .numero(rs.getLong("numero"))
                            .tipus(TipusLiniaComanda.valueOf(rs.getString("tipus")))
                            .comandaClient(infoClient.identificador())
                            .dataSolicitada(rs.getDate("data_solicitada").toLocalDate())
                            .dataPrevistaSortida(rs.getDate("data_prevista_sortida").toLocalDate())
                            .dataPrevistaSortidaInterna(MapperUtils.readOptionalDate(rs, "data_prevista_sortida_interna"))
                            .dataConfirmadaFabrica(MapperUtils.readOptionalDate(rs, "data_confirmada_fabrica"))
                            .quantitat(rs.getLong("quantitat"))
                            .preu(Preu.of(rs.getBigDecimal("preu"), rs.getString("divisa")))
                            .isPreuFixat(rs.getBoolean("preu_fixat"))
                            .versio(rs.getString("versio"))
                            .comentarisInterns(Optional.ofNullable(rs.getString("comIntern")))
                            .comentarisClient(Optional.ofNullable(rs.getString("comClient")))
                            .comandaBlanca(MapperUtils.readOptionalLong(rs, "comanda_blanca"))
                            .build();
                }
        );
    }

    @JsonDeserialize(builder = ObtenirLiniesAnuladesResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirLiniesAnuladesResponse {
        long codi();
        long numero();
        TipusLiniaComanda tipus();
        String comandaClient();
        LocalDate dataSolicitada();
        LocalDate dataPrevistaSortida();
        Optional<LocalDate> dataPrevistaSortidaInterna();
        Optional<LocalDate> dataConfirmadaFabrica();
        Preu preu();
        boolean isPreuFixat();
        long quantitat();;
        Optional<String> comentarisInterns();
        Optional<String> comentarisClient();
        Optional<Long> comandaBlanca();
        String versio();

        @Derived
        default int setmana() {
            return dataSolicitada().get(WeekFields.ISO.weekOfWeekBasedYear());
        }

        @Derived
        default String codiNumeroFormat() {
            return String.format("%07d", codi()) + " / " + String.format("%04d", numero());
        }

    }

}

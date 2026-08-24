package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.InformacioComanda;
import ames.comercial.albarans.internal.domain.linia.InformacioComandaImpl;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.server.Json;
import ames.comercial.shared.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

@Service
public class ObtenirAlbaransObertsAmbLinies {

    @Autowired NamedParameterJdbcTemplate jdbcTemplate;
    private @Autowired ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    public List<AlbaraAmbLiniesDTO> executar(String codiClient) {
        // Es consideren candidats tots els albarans oberts (no tancats) del client, independentment de la data.
        String sql = """
                SELECT *
                FROM albarans.albara a
                LEFT JOIN albarans.linia_albara l ON a.codi = l.codi_albara AND a.empresa = l.empresa
                WHERE a.client = :codiClient
                    AND NOT is_tancat
                """;
        var params = Map.of("codiClient", codiClient);
        return jdbcTemplate.query(sql, params, this::extractData);
    }

    private List<AlbaraAmbLiniesDTO> extractData(ResultSet rs) throws SQLException {
        Map<KeyAlbara, AlbaraAmbLiniesDTO> albaransMap = new HashMap<>();
        while (rs.next()) {
            var keyAlbara = KeyAlbara.of(rs.getLong("codi"), rs.getString("empresa"));
            var albara = albaransMap.get(keyAlbara);
            // Si l'albarà no existeix al map, es crea i s'afegeix al map. En cas contrari, es recupera del map per afegir les línies associades
            if (albara == null) {
                    albara = AlbaraAmbLiniesDTOImpl.builder()
                            .idAlbara(keyAlbara)
                            .data(rs.getDate("data").toLocalDate())
                            .adresa(json.deserialize(rs.getString("adresa"), Adresa.class))
                            .informacioEnviament(json.deserialize(rs.getString("informacio_enviament"), InformacioEnviament.class))
                            .linies(new ArrayList<>())
                            .build();
            }

            var numLinia = rs.getLong("linia");
            if (!rs.wasNull()) {
                // S'afegeix la línia
                albara = AlbaraAmbLiniesDTOImpl.builder()
                        .from(albara)
                        .addLinies(LiniaAlbaraDTOImpl.builder()
                                .idLiniaAlbara(KeyLiniaAlbara.of(
                                        KeyAlbara.of(rs.getLong("codi_albara"), rs.getString("empresa")),
                                        numLinia
                                ))
                                .articleClient(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
                                .infoComanda(mapInformacioComanda(rs))
                                .quantitat(rs.getLong("quantitat"))
                                .preu(Preu.of(rs.getBigDecimal("preu"), Divisa.getBySymbol(rs.getString("divisa"))))
                                .build())
                        .build();
            }

            albaransMap.put(keyAlbara, albara);

        }
        return albaransMap.values().stream().toList();
    }

    private Optional<InformacioComanda> mapInformacioComanda(ResultSet rs) throws SQLException {
        var comanda = rs.getLong("comanda");
        if (rs.wasNull()) {
            return Optional.empty();
        }
        return Optional.of(InformacioComandaImpl.builder()
                .comanda(comanda)
                .comandaClient(rs.getString("comanda_client"))
                .programa(rs.getString("programa"))
                .build());
    }

    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface AlbaraAmbLiniesDTO {
        KeyAlbara idAlbara();
        LocalDate data();
        Adresa adresa();
        InformacioEnviament informacioEnviament();
        List<LiniaAlbaraDTO> linies();

        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface LiniaAlbaraDTO {
            KeyLiniaAlbara idLiniaAlbara();
            KeyArticleClient articleClient();
            Optional<InformacioComanda> infoComanda();
            long quantitat();
            Preu preu();
        }

    }

}

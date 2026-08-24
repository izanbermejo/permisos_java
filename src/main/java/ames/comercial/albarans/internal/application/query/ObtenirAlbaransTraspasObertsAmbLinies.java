package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.internal.domain.albara.InformacioTraspas;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.server.Json;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Preu;
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

/**
 * Obté els albarans de traspàs OBERTS (no tancats) candidats a ser aprofitats per una nova proposta de traspàs,
 * juntament amb les seves línies. Un albarà de traspàs no té client (és un moviment intern); per això no es pot
 * reaprofitar la consulta d'albarans de sortida ({@link ObtenirAlbaransObertsAmbLinies}).
 * <p>
 * Es filtra per magatzem d'origen, tipus de traspàs i magatzem receptor (destí), independentment de la data.
 * El magatzem receptor i l'empresa receptora es llegeixen de la informació de traspàs (JSON) de la capçalera.
 */
@Service
public class ObtenirAlbaransTraspasObertsAmbLinies {

    @Autowired NamedParameterJdbcTemplate jdbcTemplate;
    private @Autowired ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init() {
        json = new Json(jsonMapper);
    }

    public List<AlbaraTraspasAmbLiniesDTO> executar(String magatzemOrigen, String magatzemDesti) {
        // Es consideren candidats tots els albarans de traspàs oberts (no tancats) del magatzem d'origen,
        // independentment de la data.
        String sql = """
                SELECT *
                FROM albarans.albara a
                LEFT JOIN albarans.linia_albara l ON a.codi = l.codi_albara AND a.empresa = l.empresa
                WHERE a.magatzem = :magatzemOrigen
                    AND a.client IS NULL
                    AND a.tipus IN ('TRASPAS_PLATAFORMA', 'TRASPAS_EMPRESA', 'TRASPAS_MAGATZEM', 'TRASPAS_MAGATZEM_EMPRESA')
                    AND NOT a.is_tancat
                """;
        var params = Map.of("magatzemOrigen", magatzemOrigen);
        List<AlbaraTraspasAmbLiniesDTO> tots = jdbcTemplate.query(sql, params, this::extractData);
        // Es filtren els albarans el magatzem receptor dels quals coincideix amb el destí de la proposta
        return tots.stream()
                .filter(a -> magatzemDesti.equals(a.magatzemReceptor()))
                .toList();
    }

    private List<AlbaraTraspasAmbLiniesDTO> extractData(ResultSet rs) throws SQLException {
        Map<KeyAlbara, AlbaraTraspasAmbLiniesDTO> albaransMap = new LinkedHashMap<>();
        while (rs.next()) {
            var keyAlbara = KeyAlbara.of(rs.getLong("codi"), rs.getString("empresa"));
            var albara = albaransMap.get(keyAlbara);
            if (albara == null) {
                var informacioTraspas = json.deserialize(rs.getString("informacio_traspas"), InformacioTraspas.class);
                albara = AlbaraTraspasAmbLiniesDTOImpl.builder()
                        .idAlbara(keyAlbara)
                        .data(rs.getDate("data").toLocalDate())
                        .magatzem(rs.getString("magatzem"))
                        .empresaReceptora(informacioTraspas.empresaReceptora())
                        .magatzemReceptor(informacioTraspas.magatzemReceptor())
                        .isTraspasAbonable(informacioTraspas.isTraspasAbonable())
                        .linies(new ArrayList<>())
                        .build();
            }

            var numLinia = rs.getLong("linia");
            if (!rs.wasNull()) {
                albara = AlbaraTraspasAmbLiniesDTOImpl.builder()
                        .from(albara)
                        .addLinies(LiniaAlbaraTraspasDTOImpl.builder()
                                .idLiniaAlbara(KeyLiniaAlbara.of(keyAlbara, numLinia))
                                .articleClient(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
                                .codiPartidaArantzelaria(rs.getString("pesa_codi_partida_arantzelaria"))
                                .quantitat(rs.getLong("quantitat"))
                                .preu(Preu.of(rs.getBigDecimal("preu"), Divisa.getBySymbol(rs.getString("divisa"))))
                                .build())
                        .build();
            }

            albaransMap.put(keyAlbara, albara);
        }
        return new ArrayList<>(albaransMap.values());
    }

    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface AlbaraTraspasAmbLiniesDTO {
        /** Clau de l'albarà: codi + empresa d'origen */
        KeyAlbara idAlbara();
        /** Data de l'albarà */
        LocalDate data();
        /** Magatzem d'origen */
        String magatzem();
        /** Empresa receptora (destí) */
        String empresaReceptora();
        /** Magatzem receptor (destí) */
        String magatzemReceptor();
        /** Cert si el traspàs s'ha marcat com a abonable en comptes de facturar-se */
        boolean isTraspasAbonable();
        List<LiniaAlbaraTraspasDTO> linies();

        /**
         * Cert si les línies que s'afegeixin a aquest albarà neixeran pendents de facturar i valorades amb la
         * tarifa AMES. Mateix criteri que {@code Albara.isTraspasFacturable()}: creua empreses i no és abonable.
         */
        default boolean isTraspasFacturable() {
            return !idAlbara().empresa().equals(empresaReceptora()) && !isTraspasAbonable();
        }

        @Value.Style(typeImmutable = "*Impl")
        @Value.Immutable
        interface LiniaAlbaraTraspasDTO {
            KeyLiniaAlbara idLiniaAlbara();
            KeyArticleClient articleClient();
            String codiPartidaArantzelaria();
            long quantitat();
            Preu preu();
        }
    }

}

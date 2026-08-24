package ames.comercial.albarans.internal.application.query;

import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.albara.TipusAlbara;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

/**
 * Busca els albarans d'un client que ja tenen un número d'albarà especial determinat, per poder
 * avisar l'usuari abans de crear un consum o de canviar-ne el número.
 * <p>
 * Es comproven <b>tots els tipus d'albarà</b> del client dins la mateixa empresa (el número
 * d'albarà especial és del client, i repetir-lo en una sortida o en un traspàs és igual de
 * sospitós que repetir-lo en un consum). La comparació ignora majúscules i espais.
 * <p>
 * <b>No és una validació bloquejant</b>: retorna les coincidències perquè el front les mostri i
 * l'usuari decideixi. Una llista buida vol dir que no hi ha res a avisar.
 */
@Service
public class ComprovarAlbaraEspecialClient {

    /** Les coincidències només serveixen per avisar; no cal llistar-les totes */
    private static final int MAX_COINCIDENCIES = 5;

    @Autowired NamedParameterJdbcTemplate jdbcAmes;

    /**
     * @param codiExclos albarà que no s'ha de tenir en compte (el que s'està editant); pot ser null
     */
    public List<AlbaraMateixEspecialResponse> executar(String empresa, String client,
                                                       String numeroAlbaraEspecial, Long codiExclos) {
        var valor = numeroAlbaraEspecial == null ? "" : numeroAlbaraEspecial.trim();
        // Sense número (o sense client encara) no hi ha res a comprovar
        if (valor.isEmpty() || client == null || client.isBlank()) {
            return List.of();
        }

        var params = new MapSqlParameterSource()
                .addValue("empresa", empresa, Types.VARCHAR)
                .addValue("client", client, Types.VARCHAR)
                .addValue("valor", valor, Types.VARCHAR)
                .addValue("codiExclos", codiExclos, Types.BIGINT)
                .addValue("limit", MAX_COINCIDENCIES, Types.INTEGER);
        String sql = """
                SELECT codi, empresa, tipus, data, numero_albara_especial, is_tancat, is_facturat
                FROM albarans.albara
                WHERE empresa = :empresa
                    AND client = :client
                    AND upper(trim(numero_albara_especial)) = upper(:valor)
                    AND (:codiExclos IS NULL OR codi <> :codiExclos)
                ORDER BY data DESC, codi DESC
                LIMIT :limit
                """;
        return jdbcAmes.query(sql, params, (rs, rowNum) -> AlbaraMateixEspecialResponseImpl.builder()
                .id(KeyAlbara.of(rs.getLong("codi"), rs.getString("empresa")))
                .tipus(TipusAlbara.valueOf(rs.getString("tipus")))
                .data(rs.getDate("data").toLocalDate())
                .numeroAlbaraEspecial(rs.getString("numero_albara_especial"))
                .isTancat(rs.getBoolean("is_tancat"))
                .isFacturat(rs.getBoolean("is_facturat"))
                .build());
    }

    @JsonDeserialize(builder = AlbaraMateixEspecialResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface AlbaraMateixEspecialResponse {
        KeyAlbara id();
        TipusAlbara tipus();
        LocalDate data();
        /** Número tal com està guardat a l'albarà (pot diferir en majúscules o espais del cercat) */
        String numeroAlbaraEspecial();
        boolean isTancat();
        boolean isFacturat();
    }

}

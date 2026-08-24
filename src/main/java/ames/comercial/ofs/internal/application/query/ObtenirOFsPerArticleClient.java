package ames.comercial.ofs.internal.application.query;

import ames.comercial.server.MapperUtils;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class ObtenirOFsPerArticleClient {

    @Autowired @Qualifier("jdbcAmes")
    JdbcTemplate jdbcAmes;

    public List<OrdreFabricacioResponse> get(KeyArticleClient articleClient) {
        var params = new MapSqlParameterSource("artint", articleClient.artint())
                .addValue("clicod", articleClient.clicod());

        return new NamedParameterJdbcTemplate(jdbcAmes).query(
                """
                        SELECT distinct of.*
                        FROM ofs.ordre_fabricacio of
                        JOIN ofs.termini t ON of.numero = t.numero
                        WHERE of.artint = :artint
                        AND of.clicod = :clicod
                        ORDER BY of.numero desc
                        LIMIT 100
                        
                """,
                params,
                this::mapAllOrdreFabricacio
        );
    }

    private OrdreFabricacioResponse mapAllOrdreFabricacio(ResultSet rs, int rowNum) throws SQLException {
        return OrdreFabricacioResponseImpl.builder()
                .numero(rs.getLong("numero"))
                .dataEmissio(rs.getDate("data_emissio").toLocalDate())
                .ofAnterior(MapperUtils.readOptionalLong(rs, "of_anterior"))
                .ofPosterior(MapperUtils.readOptionalLong(rs, "of_posterior"))
                .dataAnulacio(MapperUtils.readOptionalDate(rs, "data_anulacio"))
                .quantitatTotal(rs.getLong("quantitat"))
                .quantitatRebudaTerminis(rs.getLong("quantitat_rebuda_terminis"))
                .exces(rs.getLong("quantitat_exces"))
                .quantitatRebudaEntrades(rs.getLong("quantitat_rebuda_entrades"))
                .quantitatRebudaEntradesTotal(rs.getLong("quantitat_rebuda_entrades_total"))
                .increment(rs.getBoolean("existeix_increment"))
                .canviFabrica(rs.getBoolean("canvi_fabrica"))
                .fabrica(rs.getString("fabrica"))
                .diesCalculIncrement(rs.getInt("dies_calcul_increment"))
                .build();
    }

    @JsonDeserialize(builder = OrdreFabricacioResponseImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface OrdreFabricacioResponse {

        long numero();
        LocalDate dataEmissio();
        Optional<Long> ofAnterior();
        Optional<Long> ofPosterior();
        Optional<LocalDate> dataAnulacio();
        long quantitatTotal();
        long quantitatRebudaTerminis();
        long exces();
        long quantitatRebudaEntrades();
        long quantitatRebudaEntradesTotal();
        Optional<Boolean> increment();
        Optional<Boolean> canviFabrica();
        String fabrica();
        Optional<Integer> diesCalculIncrement();

        @Value.Derived
        default boolean isAnulada() {
            return dataAnulacio().isPresent();
        }

        @Value.Derived
        default long quantitatPendent() {
            return Math.max(0,  quantitatTotal() - quantitatRebudaEntrades());
        }
    }
}
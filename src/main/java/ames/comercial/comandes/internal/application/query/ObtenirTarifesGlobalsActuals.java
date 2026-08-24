package ames.comercial.comandes.internal.application.query;

import ames.comercial.shared.Divisa;
import ames.comercial.shared.Empresa;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ObtenirTarifesGlobalsActuals {

    @Autowired JdbcTemplate jdbc;

    public Optional<ObtenirTarifesGlobalsActualsResp> executar(Empresa empresa, Divisa divisa) {
        try {
            return Optional.ofNullable(jdbc.queryForObject("""
                            SELECT * FROM comandes.tarifes_globals_actuals
                            WHERE empresa = ? AND divisa = ?
                            """,
                    (resultSet, rowNum) -> {
                        return ObtenirTarifesGlobalsActualsRespImpl.builder()
                                .coixinetsWeb(readSafe(resultSet.getString("coixinets_web")))
                                .barresWeb(readSafe(resultSet.getString("barres_web")))
                                .ibinsa(readSafe(resultSet.getString("ibinsa")))
                                .filtresBxxDistribuidor(readSafe(resultSet.getString("filtres_bxx_dist")))
                                .filtresSsuDistribuidor(readSafe(resultSet.getString("filtres_ssu_dist")))
                                .filtresSxxDistribuidor(readSafe(resultSet.getString("filtres_sxx_dist")))
                                .filtresBxxClient(readSafe(resultSet.getString("filtres_bxx_cli")))
                                .filtresSsuClient(readSafe(resultSet.getString("filtres_ssu_cli")))
                                .filtresSxxClient(readSafe(resultSet.getString("filtres_sxx_cli")))
                                .filtresSsuPlaques(readSafe(resultSet.getString("filtres_ssu_p")))
                                .build();
                    }, empresa.clau(), divisa.symbol()));
        } catch (EmptyResultDataAccessException empty) {
            return Optional.empty();
        }
    }

    private Optional<String> readSafe(String value) {
        return value != null ? Optional.of(value) : Optional.empty();
    }

    @JsonDeserialize(builder = ObtenirTarifesGlobalsActualsRespImpl.Builder.class)
    @Value.Style(typeImmutable = "*Impl")
    @Value.Immutable
    public interface ObtenirTarifesGlobalsActualsResp {
        Optional<String> coixinetsWeb();
        Optional<String> barresWeb();
        Optional<String> ibinsa();
        Optional<String> filtresBxxDistribuidor();
        Optional<String> filtresSsuDistribuidor();
        Optional<String> filtresSxxDistribuidor();
        Optional<String> filtresBxxClient();
        Optional<String> filtresSsuClient();
        Optional<String> filtresSxxClient();
        Optional<String> filtresSsuPlaques();
    }


}

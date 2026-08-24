package ames.comercial.edi.internal.infraestructure.query;

import ames.comercial.edi.beans.ComandaMissatgeEDI;
import ames.comercial.edi.internal.domain.programaentrega.CapsaleraEdi;
import ames.comercial.edi.internal.domain.programaentrega.ComandaEdi;
import ames.comercial.server.Json;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ProgramaEntregaEdiRepositorySQL implements CapsaleraEdiRepository, ComandaEdiRepository {

    private static final String SQL = "SELECT json FROM edi.comanda WHERE path_edi = ?";

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
    @Autowired ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init() {
        json = new Json(jsonMapper);
    }

    private List<ComandaMissatgeEDI> obtenirMissatge(String pathEdi) {
        List<String> jsonStrings = jdbcAmes.queryForList(SQL, String.class, pathEdi);

        return jsonStrings.stream()
                .map(jsonStr -> json.deserialize(jsonStr, ComandaMissatgeEDI.class))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public List<CapsaleraEdi> obtenirCapsaleraEdi(String pathEdi) {
        return obtenirMissatge(pathEdi).stream()
                .map(CapsaleraEdi::new)
                .toList();
    }

    @Override
    public List<ComandaEdi> obtenirComandesPerMissatge(String pathEdi) {
        return obtenirMissatge(pathEdi).stream()
                .flatMap(m -> Optional.ofNullable(m.getLineas())
                        .orElse(List.of())
                        .stream())
                .map(ComandaEdi::new)
                .toList();
    }
}

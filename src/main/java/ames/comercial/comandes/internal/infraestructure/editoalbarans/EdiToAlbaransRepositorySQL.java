package ames.comercial.comandes.internal.infraestructure.editoalbarans;

import ames.comercial.edi.beans.ComandaMissatgeEDI;
import ames.comercial.server.Json;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class EdiToAlbaransRepositorySQL implements EdiToAlbaransRepository {

    private @Autowired
    @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
    private @Autowired ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    @Override
    public void save(KeyArticleClient articleClient, String comandaClient, ComandaMissatgeEDI missatge) {
        var params = new HashMap<String,Object>() {{
            put("artint", articleClient.artint());
            put("clicod", articleClient.clicod());
            put("comanda_client", comandaClient);
            put("missatge", json.serialize(missatge));
            put("datareg", LocalDateTime.now());
            put("dataprocessat", null);
        }};
        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("comandes")
                .withTableName("edi_to_albarans")
                .execute(params);
    }

    @Override
    public List<EdiToAlbaransRecord> pendingEdiToAlbarans() {
        return jdbcAmes.query("SELECT * FROM comandes.edi_to_albarans WHERE dataprocessat IS NULL ORDER by datareg ASC",
                (rs) -> {
                    List<EdiToAlbaransRecord> results = new ArrayList<>();
                    while (rs.next()) {
                        results.add(new EdiToAlbaransRecord(
                                KeyArticleClient.of(rs.getString("artint"),rs.getString("clicod")),
                                rs.getString("comanda_client"),
                                json.deserialize(rs.getString("missatge"), ComandaMissatgeEDI.class),
                                rs.getTimestamp("datareg").toLocalDateTime()));
                    }
                    return results;
                }
        );
    }

    @Override
    public void marcaProcessat(EdiToAlbaransRecord reg) {
        jdbcAmes.update("""
            UPDATE comandes.edi_to_albarans SET dataprocessat = ?
            WHERE artint = ? AND clicod = ? AND comanda_client = ? AND datareg = ?
            """, LocalDateTime.now(), reg.articleClient().artint(), reg.articleClient().clicod(), reg.comandaClient(),
                reg.datareg());
    }


}

package ames.comercial.edi2.internal.infraestructure.configuracio.entradesedi;

import ames.comercial.edi2.internal.domain.ConfiguracioEntradaComanda;
import ames.comercial.server.Json;
import ames.comercial.server.SimpleJdbcUpsert;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;

@Repository
public class ConfiguracioEntradesEntradesEdiSql implements ConfiguracioEntradesEdiRepository {

    @Autowired JdbcTemplate jdbcAmes;
    @Autowired private ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    @Override
    public void save(ConfiguracioEntradaComanda capsaleresEdi){
        var params = new HashMap<String, Object>();
        params.put("codi_client", capsaleresEdi.codiClient());
        params.put("tipus_missatge", capsaleresEdi.tipusMissatge());
        params.put("ferm_orientatiu", capsaleresEdi.fermOrientatiu());
        params.put("edibox", capsaleresEdi.ediBox());
        params.put("nad02", capsaleresEdi.nad02());
        params.put("codi_proveidor", capsaleresEdi.codiProveidor());
        params.put("dies_sortida", capsaleresEdi.informacioSortida().diesSortida().toArray(new Integer[0]));
        params.put("dies_restar", capsaleresEdi.informacioSortida().diesRestar());
        params.put("considerar_albarans", capsaleresEdi.considerarAlbarans());
        params.put("considerar_dues_dates", capsaleresEdi.considerarDuesDates());
        params.put("estrategia_edi", capsaleresEdi.estrategiaEdi());
        params.put("dies_tall", capsaleresEdi.diesTall());
        params.put("lloc_entrega", capsaleresEdi.llocsEntrega().toArray(new String[0]));
        params.put("tipus_document_edi", capsaleresEdi.tipusDocumentEdi().orElse(null));
        params.put("comentaris", capsaleresEdi.comentaris().orElse(null));
        params.put("is_actiu", capsaleresEdi.isActiu());

        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("edi2")
                .withTableName("configuracio_client")
                .onConflictColumns("codi_client", "tipus_missatge")
                .execute(params);
    }

    @Override
    public List<ConfiguracioEntradaComanda> obtenirConfiguracioEdi(String ediBox, String nad02, String codiProveidor, String tipus_missatge){
        return  jdbcAmes.query("""
                    SELECT *
                    FROM edi2.configuracio_client
                    WHERE edibox = ?
                    AND nad02 = ?
                    AND codi_proveidor = ?
                    AND tipus_missatge = ?
            """, new ConfiguracioEntradesEdiMapper(), ediBox, nad02, codiProveidor, tipus_missatge);
    }

    @Override
    public ConfiguracioEntradaComanda obtenirConfiguracioEdi(String codiClient, String tipusMissatge){
        return jdbcAmes.queryForObject("""
            SELECT *
            FROM edi2.configuracio_client
            WHERE codi_client = ?
            AND tipus_missatge = ?;
        """, new ConfiguracioEntradesEdiMapper(), codiClient, tipusMissatge);
    }

    @Override
    public List<ConfiguracioEntradaComanda> obtenirConfiguracioEdi(String codiClient){
        return jdbcAmes.query("""
                    SELECT *
                    FROM edi2.configuracio_client
                    WHERE codi_client = ?;
                """, new ConfiguracioEntradesEdiMapper(), codiClient);
    }
}

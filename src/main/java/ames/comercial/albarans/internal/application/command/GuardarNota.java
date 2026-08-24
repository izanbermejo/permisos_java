package ames.comercial.albarans.internal.application.command;

import ames.comercial.server.SimpleJdbcUpsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
public class GuardarNota {
    @Autowired
    JdbcTemplate jdbcAmes;

    @Transactional
    public void executar(Long codi, String empresa, String artint, String clicod, String nota) {
        var params = new HashMap<String, Object>();
        params.put("codi_albara", codi);
        params.put("empresa", empresa);
        params.put("artint", artint);
        params.put("clicod", clicod);
        params.put("nota", nota);

        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("albarans")
                .withTableName("nota_albara_pesa")
                .onConflictColumns("codi_albara", "empresa", "artint", "clicod")
                .execute(params);
    }
}

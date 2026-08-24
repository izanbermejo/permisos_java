package ames.comercial.entrades.internal.infraestructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class InboxEntradesErrorJsonSql implements  InboxEntradesErrorJson {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public void save(String contingut) {
        var params = new HashMap<String, Object>();
        params.put("contingut", contingut);

        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("entrades")
                .withTableName("inbox_error_json")
                .usingGeneratedKeyColumns("datareg")
                .execute(params);
    }

}

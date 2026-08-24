package ames.comercial.edi2.internal.infraestructure.embalatgeexpedicio;

import ames.comercial.edi2.internal.domain.embalatgeexpedicio.ElementEmbalatgeExpedicio;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.EmbalatgeExpedicio;
import ames.comercial.server.Json;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Optional;

@Repository
public class EmbalatgeExpedicioSql implements EmbalatgeExpedicioRepository {

    @Autowired JdbcTemplate jdbcAmes;
    @Autowired private ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    @Override
    public void save(KeyArticleClient articleClient, EmbalatgeExpedicio embalatgeExpedicio){

        deleteElementsEmbalatge(articleClient);

        if (embalatgeExpedicio.nivell1Retornable().isPresent())
            insertElementEmbalatge(articleClient, embalatgeExpedicio.nivell1Retornable().get());
        if (embalatgeExpedicio.nivell2Retornable().isPresent())
            insertElementEmbalatge(articleClient, embalatgeExpedicio.nivell2Retornable().get());
        if (embalatgeExpedicio.nivell3Retornable().isPresent())
            insertElementEmbalatge(articleClient, embalatgeExpedicio.nivell3Retornable().get());
        if (embalatgeExpedicio.nivell4Retornable().isPresent())
            insertElementEmbalatge(articleClient, embalatgeExpedicio.nivell4Retornable().get());
        if (embalatgeExpedicio.nivell5Retornable().isPresent())
            insertElementEmbalatge(articleClient, embalatgeExpedicio.nivell5Retornable().get());
        if (embalatgeExpedicio.nivell1NoRetornable().isPresent())
            insertElementEmbalatge(articleClient, embalatgeExpedicio.nivell1NoRetornable().get());
        if (embalatgeExpedicio.nivell2NoRetornable().isPresent())
            insertElementEmbalatge(articleClient, embalatgeExpedicio.nivell2NoRetornable().get());
        if (embalatgeExpedicio.nivell3NoRetornable().isPresent())
            insertElementEmbalatge(articleClient, embalatgeExpedicio.nivell3NoRetornable().get());
        if (embalatgeExpedicio.nivell4NoRetornable().isPresent())
            insertElementEmbalatge(articleClient, embalatgeExpedicio.nivell4NoRetornable().get());
        if (embalatgeExpedicio.nivell5NoRetornable().isPresent())
            insertElementEmbalatge(articleClient, embalatgeExpedicio.nivell5NoRetornable().get());
    }

    private void insertElementEmbalatge(KeyArticleClient articleClient, ElementEmbalatgeExpedicio element) {

        var params = new HashMap<String, Object>();
        params.put("artint", articleClient.artint());
        params.put("clicod", articleClient.clicod());
        params.put("nivell", element.tipus().nivell());
        params.put("is_retornable", element.isRetornable());
        params.put("referencia", element.referencia());
        params.put("descripcio", element.descripcio());
        params.put("codi_element", element.codiElement());
        params.put("factor_multiplicador", element.factorMultiplicador().orElse(null));
        params.put("elements_fixes", element.numElementsFixes().orElse(null));
        params.put("usuari", RequestThread.nomUsuari());
        params.put("datareg", RequestThread.dateLocal());


        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("edi2")
                .withTableName("embalatge_expedicio")
                .execute(params);
    }

    private void deleteElementsEmbalatge(KeyArticleClient articleClient) {
        jdbcAmes.update("DELETE FROM edi2.embalatge_expedicio WHERE artint = ? AND clicod = ?", articleClient.artint(), articleClient.clicod());
    }

    @Override
    public Optional<EmbalatgeExpedicio> find(KeyArticleClient articleClient){
        EmbalatgeExpedicio result = jdbcAmes.query("""
                    SELECT *
                    FROM edi2.embalatge_expedicio
                    WHERE artint = ?
                    AND clicod = ?
            """, new EmbalatgeExpedicioMapper(json), articleClient.artint(), articleClient.clicod());
        return Optional.ofNullable(result);
    }

}

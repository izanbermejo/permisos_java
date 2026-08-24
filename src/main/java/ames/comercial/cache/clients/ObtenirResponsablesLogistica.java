package ames.comercial.cache.clients;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObtenirResponsablesLogistica implements IObtenirResponsablesLogistica {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public List<String> executar() {
        return jdbcAmes.queryForList(
            """
                SELECT DISTINCT(responsable)
                FROM cache.cache_client
                WHERE responsable <> ''
                    AND responsable <> 'ORTEGA'     -- L'Ortega no és responsable però té assignats alguns clients per utillatges
                    AND estat = 'A'                 -- Només dels actius
            """,
            String.class
        );
    }

}

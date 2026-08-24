package ames.comercial.comandes.internal.infraestructure.comandacomentaris;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ComandaComentarisRepositorySQL implements ComandaComentarisRepository {

    @Autowired JdbcTemplate jdbcTemplate;

    @Override
    public void updateComentaris(long comanda, String text) {
        jdbcTemplate.update("""
                INSERT INTO comandes.comanda_comentaris (comanda, intern)
                VALUES (?, ?)
                ON CONFLICT (comanda)
                DO UPDATE SET intern = EXCLUDED.intern;
                """, comanda, text);
    }

}

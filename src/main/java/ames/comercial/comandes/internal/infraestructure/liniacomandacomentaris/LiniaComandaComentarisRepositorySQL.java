package ames.comercial.comandes.internal.infraestructure.liniacomandacomentaris;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LiniaComandaComentarisRepositorySQL implements LiniaComandaComentarisRepository {

    @Autowired JdbcTemplate jdbcTemplate;

    @Override
    public void updateComentarisInterns(KeyLiniaComanda clauLinia, String text) {
        jdbcTemplate.update("""
                INSERT INTO comandes.linia_comanda_comentaris (comanda, numero, intern)
                VALUES (?, ?, ?)
                ON CONFLICT (comanda, numero)
                DO UPDATE SET intern = EXCLUDED.intern;
                """, clauLinia.comanda(), clauLinia.numero(), text);
    }

    @Override
    public void updateComentarisClient(KeyLiniaComanda clauLinia, String text) {
        jdbcTemplate.update("""
                INSERT INTO comandes.linia_comanda_comentaris (comanda, numero, client)
                VALUES (?, ?, ?)
                ON CONFLICT (comanda, numero)
                DO UPDATE SET client = EXCLUDED.client;
                """, clauLinia.comanda(), clauLinia.numero(), text);
    }

}

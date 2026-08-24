package ames.comercial.edi2.internal.infraestructure.linia;

import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class LiniaEdiRepositorySQL implements LiniaEdiRepository {

    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired @Qualifier("jdbcNamingAmes") NamedParameterJdbcTemplate jdbcNamedAmes;


    @Override
    public long nextID(){
        return jdbcNamedAmes.queryForObject("SELECT nextval('edi2.linia_id_seq'::regclass)", new MapSqlParameterSource(), Long.class);
    }

    @Override
    public void updateComentarisInterns(KeyComandaEdi clauLinia, long idLinia, String text) {
        jdbcTemplate.update("""
                UPDATE edi2.linia
                SET comentari_intern = ?
                WHERE id_comanda = ? and id_missatge = ? and id_linia = ?;
                """, text, clauLinia.idComanda(), clauLinia.idMissatge(), idLinia);
    }

    @Override
    public void updateComentarisClient(KeyComandaEdi clauLinia, long idLinia, String text) {
        jdbcTemplate.update("""
                UPDATE edi2.linia
                SET comentari_client = ?
                WHERE id_comanda = ? and id_missatge = ? and id_linia = ?;
                """, text, clauLinia.idComanda(), clauLinia.idMissatge(), idLinia);
    }

    @Override
    public void updateComentarisInterns(KeyComandaEdi clauLinia, String text) {
        jdbcTemplate.update("""
                UPDATE edi2.linia
                SET comentari_intern = ?
                WHERE id_comanda = ? and id_missatge = ?;
                """, text, clauLinia.idComanda(), clauLinia.idMissatge());
    }

    @Override
    public void updateComentarisClient(KeyComandaEdi clauLinia, String text) {
        jdbcTemplate.update("""
                UPDATE edi2.linia
                SET comentari_client = ?
                WHERE id_comanda = ? and id_missatge = ?;
                """, text, clauLinia.idComanda(), clauLinia.idMissatge());
    }

}

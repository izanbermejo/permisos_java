package ames.comercial.comandes.internal.infraestructure.entradanormalitzat;

import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class EntradaNormalitzatRepositorySql implements EntradaNormalitzatRepository {

    @Autowired JdbcTemplate jdbcAmes;

    @Override
    public void save(KeyArticleClient articleClient, Empresa empresa) {
        jdbcAmes.update("""
                INSERT INTO comandes.entrada_normalitzat (artint, clicod, empresa)
                VALUES (?, ?, ?)
                ON CONFLICT (artint, clicod, empresa)
                DO NOTHING
                """, articleClient.artint(), articleClient.clicod(), empresa.clau());
    }

    @Override
    public List<Pair<KeyArticleClient, Empresa>> obtenirPendents() {
        return jdbcAmes.query(
                "SELECT * FROM comandes.entrada_normalitzat",
                rs -> {
                    List<Pair<KeyArticleClient, Empresa>> list = new ArrayList<>();
                    while (rs.next()) {
                        list.add(new Pair<>(
                                KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")),
                                Empresa.getByClau(rs.getString("empresa")))
                        );
                    }
                    return list;
                }
        );
    }
    
    @Override
    public void delete(KeyArticleClient articleClient, Empresa empresa) {
        jdbcAmes.update("""
                DELETE FROM comandes.entrada_normalitzat WHERE artint = ? AND clicod = ? and empresa = ?
                """, articleClient.artint(), articleClient.clicod(), empresa.clau());
    }

}

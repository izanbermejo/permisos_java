package ames.comercial.edi2.internal.infraestructure.inbox;

import ames.comercial.edi2.internal.domain.Inbox;
import ames.comercial.edi2.internal.infraestructure.inbox.mapper.InboxMapper;
import ames.comercial.server.Json;
import ames.comercial.server.SimpleJdbcUpsert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InboxEdiSql implements InboxEdiRepository {

    @Autowired JdbcTemplate jdbcAmes;
    @Autowired @Qualifier("jdbcNamingAmes") NamedParameterJdbcTemplate jdbcNamedAmes;
    private Json json;

    @Override
    public int nextId(){
        return jdbcNamedAmes.queryForObject("SELECT nextval('edi2.inbox_id_seq')", new MapSqlParameterSource(), Integer.class);
    }

    @Override
    public Optional<Inbox> get(long id) {
        try {
            return Optional.ofNullable(jdbcAmes.queryForObject(
                    "SELECT * FROM edi2.inbox WHERE id = ?",
                    new InboxMapper(json),
                    id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(String missatge) {
        return Boolean.TRUE.equals(jdbcAmes.queryForObject("SELECT EXISTS(SELECT 1 FROM edi2.inbox WHERE missatge = ?)", Boolean.class, missatge));
    }

    private void delete(long id) {
        jdbcAmes.update("DELETE FROM edi2.inbox WHERE id = ?", id);
    }

    @Override
    public void save(Inbox inbox) {
        var params = new HashMap<String, Object>();
        params.put("id", inbox.id());
        params.put("missatge", inbox.missatge());
        params.put("path", inbox.path());
        params.put("path_pdf", inbox.pathPDF().orElse(null));
        params.put("datareg", inbox.dataReg());
        params.put("data_processat", inbox.dataProcessat().orElse(null));
        params.put("contingut", inbox.contingut());
        params.put("nom_pdf", inbox.nomPdf().orElse(null));

        new SimpleJdbcUpsert(jdbcAmes)
                .withSchemaName("edi2")
                .withTableName("inbox")
                .onConflictColumns("id")
                .execute(params);
    }

    @Override
    public void marcaPdfLligat(long id, String path) {
        jdbcAmes.update("""
        UPDATE edi2.inbox
        SET path_pdf = ?
        WHERE id = ?
    """, path, id);
    }

    @Override
    public void marcaProcessat(long id) {
        jdbcAmes.update("UPDATE edi2.inbox SET data_processat = now() WHERE id = ?", id);
    }

    @Override
    public void marcaError(long id, String error) {
        jdbcAmes.update("UPDATE edi2.inbox SET data_processat = now(), error = ? WHERE id = ?", error, id);
    }

    @Override
    public Map<Integer, String> obtenirPendentsPerLligar() {
        //S'obtenen els registres que estan pendents per lligar amb el pdf corresponent. No té en compte els registres de més de 7 dies.
        return jdbcAmes.query(
            """
                SELECT id, missatge
                FROM edi2.inbox
                WHERE path_pdf IS NULL
                AND datareg::date >= current_date - 7;
            """,rs -> {
                Map<Integer, String> map = new HashMap<>();
                while (rs.next()) {
                    map.put(rs.getInt("id"), rs.getString("missatge"));
                }
                return map;
            }
        );
    }

    @Override
    public Map<Integer, String> obtenirPendentsPerProcessar(){
        return jdbcAmes.query(
            """
                SELECT i.id, i.contingut
                FROM edi2.inbox i
                WHERE data_processat IS NULL
                AND datareg::date >= current_date - 7;
            """,rs -> {
                Map<Integer, String> map = new HashMap<>();
                while (rs.next()) {
                    map.put(rs.getInt("id"), rs.getString("contingut"));
                }
                return map;
            }
        );
    }
}

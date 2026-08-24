package ames.comercial.magatzem.internal.infraestructure;

import ames.comercial.magatzem.internal.domain.Magatzem;
import ames.comercial.magatzem.internal.domain.MagatzemImpl;
import ames.comercial.magatzem.internal.domain.TipusMagatzem;
import ames.comercial.server.Json;
import ames.comercial.shared.Adresa;
import ames.comercial.shared.InformacioEnviament;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public class MagatzemRepositorySQL implements MagatzemRepository {

    private @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;
    private @Autowired ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init() {
        json = new Json(jsonMapper);
    }

    @Override
    public Optional<Magatzem> find(String codi) {
        try {
            return Optional.of(jdbcAmes.queryForObject(
                    "SELECT * FROM com_magatzem.magatzem WHERE codi = ?", mapper(), codi));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Magatzem> findAll() {
        return jdbcAmes.query("SELECT * FROM com_magatzem.magatzem", mapper());
    }

    @Override
    public void save(Magatzem m) {
        var params = new HashMap<String, Object>();
        params.put("codi", m.codi());
        params.put("tipus", m.tipus().name());
        params.put("descripcio", m.descripcio());
        params.put("adresa", json.serialize(m.adresa()));
        params.put("informacio_enviament", json.serialize(m.informacioEnviament().orElse(null)));
        params.put("dies_transport", m.diesTransport());
        params.put("is_facturable", m.isFacturable());
        params.put("is_actiu", m.isActiu());
        params.put("is_sii", m.isSii());
        params.put("usuari_responsable", m.usuariResponsable().orElse(null));
        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("com_magatzem")
                .withTableName("magatzem")
                .execute(params);
    }

    @Override
    public void deleteAll() {
        jdbcAmes.update("DELETE FROM com_magatzem.magatzem");
    }

    @Override
    public Optional<String> findCodiIntermig(String magatzemInicial, String magatzemFinal) {
        try {
            return Optional.ofNullable(jdbcAmes.queryForObject("""
                    SELECT intermig FROM com_magatzem.magatzems_intermitjos
                    WHERE inicial = ? AND final = ?
                    """, String.class, magatzemInicial, magatzemFinal));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private RowMapper<Magatzem> mapper() {
        return (rs, rowNum) -> MagatzemImpl.builder()
                .codi(rs.getString("codi"))
                .tipus(TipusMagatzem.valueOf(rs.getString("tipus")))
                .descripcio(rs.getString("descripcio"))
                .adresa(json.deserialize(rs.getString("adresa"), Adresa.class))
                .informacioEnviament(Optional.ofNullable(
                        json.deserialize(rs.getString("informacio_enviament"), InformacioEnviament.class)))
                .diesTransport(rs.getLong("dies_transport"))
                .isFacturable(rs.getBoolean("is_facturable"))
                .isActiu(rs.getBoolean("is_actiu"))
                .isSii(rs.getBoolean("is_sii"))
                .usuariResponsable(Optional.ofNullable(rs.getString("usuari_responsable")))
                .build();
    }

}

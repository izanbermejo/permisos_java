package ames.comercial.edi2.internal.infraestructure.comanda;

import ames.comercial.edi2.internal.domain.linia.bloc.DR;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi.Estat;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdiImpl;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.edi2.internal.domain.linia.LiniaEdi;
import ames.comercial.server.Json;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;

@Repository
public class ComandaEdiSql implements ComandaEdiRepository {

    @Autowired JdbcTemplate jdbcAmes;
    @Autowired NamedParameterJdbcTemplate jdbc;
    @Autowired private ObjectMapper jsonMapper;
    private Json json;

    @PostConstruct
    public void init () {
        json = new Json(jsonMapper);
    }

    @Override
    public void save(List<ComandaEdi> comandes){
        comandes.forEach(this::save);
    }

    @Override
    public void save(ComandaEdi comanda){
        delete(KeyComandaEdi.of(comanda.idMissatge(), comanda.idComanda()));

        var params = new HashMap<String, Object>();
        params.put("id_missatge", comanda.idMissatge());
        params.put("id_capsalera", comanda.idCapsalera());
        params.put("id_comanda", comanda.idComanda());
        params.put("artint", comanda.artInt().orElse(null));
        params.put("clicod", comanda.cliCod().orElse(null));
        params.put("la", json.serialize(comanda.la()));
        params.put("lb", comanda.lb().map(json::serialize).orElse(null));
        params.put("lc", json.serialize(comanda.lc()));
        params.put("ld", comanda.ld().map(json::serialize).orElse(null));
        params.put("ls", comanda.ls().map(json::serialize).orElse(null));
        params.put("le", comanda.le().isEmpty() ? null : json.serialize(comanda.le()));
        params.put("lt", comanda.lt().isEmpty() ? null : json.serialize(comanda.lt()));
        params.put("lg", comanda.lg().map(json::serialize).orElse(null));
        params.put("lh", comanda.lh().map(json::serialize).orElse(null));
        params.put("li", comanda.li().map(json::serialize).orElse(null));
        params.put("ll", comanda.ll().isEmpty() ? null : json.serialize(comanda.ll()));
        params.put("lq", comanda.lq().isEmpty() ? null : json.serialize(comanda.lq()));
        params.put("aa", comanda.aa().isEmpty() ? null : json.serialize(comanda.aa()));
        params.put("estat", comanda.estat());
        params.put("error", comanda.error().orElse(null));

        comanda.linies().forEach(liniaEdi -> saveLinies(comanda.idComanda(),comanda.idMissatge(), liniaEdi));

        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("edi2")
                .withTableName("comanda")
                .execute(params);
    }

    private void delete(KeyComandaEdi keyComandaEdi) {
        jdbcAmes.update("DELETE FROM edi2.comanda WHERE id_missatge = ? and id_comanda = ?", keyComandaEdi.idMissatge(), keyComandaEdi.idComanda());
        jdbcAmes.update("DELETE FROM edi2.linia WHERE id_missatge = ? and id_comanda = ?", keyComandaEdi.idMissatge(), keyComandaEdi.idComanda());
    }

    private void saveLinies(long idComanda, long idMissatge, LiniaEdi liniaEdi){
        var params = new HashMap<String, Object>();
        params.put("id_linia", nextID());
        params.put("id_comanda", idComanda);
        params.put("id_missatge", idMissatge);
        params.put("da_tipo_detalle",liniaEdi.da().tipoDetalle());
        params.put("da_cantidad", liniaEdi.da().cantidad());
        params.put("da_unidad_medida", liniaEdi.da().unidadMedida());
        params.put("da_fecha_inicial", liniaEdi.da().fechaInicial().orElse(null));
        params.put("da_hora_inicial", liniaEdi.da().horaInicial().orElse(null));
        params.put("da_fecha_final", liniaEdi.da().fechaFinal().orElse(null));
        params.put("da_hora_final", liniaEdi.da().horaFinal().orElse(null));
        params.put("da_razon_instruccion", liniaEdi.da().razonInstruccion().orElse(null));
        params.put("da_numero_ran", liniaEdi.da().numeroRan().orElse(null));
        params.put("da_fecha_ran", liniaEdi.da().fechaRan().orElse(null));
        params.put("da_frecuencia_envio", liniaEdi.da().frecuenciaEnvio().orElse(null));
        params.put("da_num_tarjeta_kanban", liniaEdi.da().numTarjetaKanban().orElse(null));
        params.put("da_ultimo_numero_ran_emitido", liniaEdi.da().ultimoNumeroRanEmitido().orElse(null));
        params.put("da_filler", null);
        params.put("dr_fecha_entrada", liniaEdi.dr().flatMap(DR::fechaEntradaLinea).orElse(null));
        params.put("dr_hora_entrada", liniaEdi.dr().flatMap(DR::horaEntradaLinea).orElse(null));
        params.put("dr_filler", null);

        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("edi2")
                .withTableName("linia")
                .execute(params);
    }

    private int nextID(){
        return jdbc.queryForObject("SELECT nextval('edi2.linia_id_seq')", new MapSqlParameterSource(), Integer.class);
    }

    @Override
    public Optional<ComandaEdi> obtenirComanda(KeyComandaEdi key) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idMissatge", key.idMissatge())
                .addValue("idComanda", key.idComanda());

        //recupero primer les linies, ja que per algunes comandes ens envien el nom de la comanda del sistema alla
        // y es necessaria per fer el derived del numero de la comanda

        List<LiniaEdi> linies = jdbc.query("""
            SELECT *
            FROM edi2.linia
            WHERE id_missatge = :idMissatge AND id_comanda = :idComanda
        """, params, new LiniaEdiMapper());

        Optional<ComandaEdi> comanda = Optional.ofNullable(jdbc.queryForObject("""
            SELECT *
            FROM edi2.comanda
            WHERE id_missatge = :idMissatge
              AND id_comanda = :idComanda
        """, params, new ComandaEdiMapper(json, linies)));

        if (comanda.isEmpty()) return Optional.empty();

        return Optional.of(ComandaEdiImpl.builder()
            .from(comanda.get())
            .linies(linies)
            .build());
    }

    public List<ComandaEdi> obtenirComandabyNumComanda(String numComanda) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("numComanda", numComanda);

        List<ComandaEdi> comanda = jdbc.query("""
                    SELECT *
                    FROM edi2.comanda
                    WHERE lg ->> 'numeroContratoPedido' = :numComanda
                    ORDER BY id_missatge ASC;
                """, params, new ComandaEdiMapper(json));

        if (comanda.isEmpty()) return List.of();

        return comanda;
    }

    @Override
    public List<KeyComandaEdi> obtenirComandesPerLligar() {
        return jdbcAmes.query("""
        SELECT id_missatge, id_comanda
        FROM edi2.comanda
        WHERE estat = 'PENDENT_LLIGAR';
        """,
                (rs, rowNum) -> KeyComandaEdi.of(
                        rs.getLong("id_missatge"),
                        rs.getLong("id_comanda")
                )
        );
    }

    @Override
    public List<ComandaEdi> obtenirComandesPerMissatge(long idMissatge) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("idMissatge", idMissatge);

        List<LiniaEdi> toutesLinies = jdbc.query("""
            SELECT *
            FROM edi2.linia
            WHERE id_missatge = :idMissatge
            ORDER BY id_comanda, da_fecha_final
        """, params, new LiniaEdiMapper());

        Map<Long, List<LiniaEdi>> liniesByComanda = toutesLinies.stream()
                .collect(java.util.stream.Collectors.groupingBy(LiniaEdi::idComanda));

        List<ComandaEdi> comandes = jdbc.query("""
            SELECT *
            FROM edi2.comanda
            WHERE id_missatge = :idMissatge
            ORDER BY id_comanda
        """, params, new ComandaEdiMapper(json));

        List<ComandaEdi> resultat = new ArrayList<>();
        comandes.forEach(c -> resultat.add(ComandaEdiImpl.builder()
                        .from(c)
                        .linies(liniesByComanda.getOrDefault(c.idComanda(), List.of()))
                        .build()));
        return resultat;
    }

    @Override
    public void marcaEstatComanda(long idMissatge, long idComanda, Estat estat, Optional<String> missatge) {
        jdbcAmes.update("""
            UPDATE edi2.comanda
            SET estat = ?,
                error = ?
            WHERE id_comanda = ?
              AND id_missatge = ?
            """, estat.name(), missatge.orElse(null), idComanda, idMissatge);
    }
}

package ames.comercial.comandes.internal.infraestructure.variacio;

import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.LiniaComanda;
import ames.comercial.shared.Divisa;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class VariacioComandaSQL implements VariacioComandaRepository {

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

    @Override
    public LocalDateTime maxDatareg() {
        return jdbcAmes.queryForObject("SELECT coalesce(MAX(datareg),now()) FROM comandes.variacio", LocalDateTime.class);
    }

    @Override
    public void insert(LiniaComanda liniaComanda, LocalDateTime datareg, LocalDate dataVariacio) {
        insertIntern(liniaComanda, datareg, dataVariacio, false);
    }

    @Override
    public void insertNegatiu(LiniaComanda liniaComanda, LocalDateTime datareg, LocalDate dataVariacio) {
        insertIntern(liniaComanda, datareg, dataVariacio, true);
    }

    @Override
    public List<VariacioBuidaReq> getPendentsComplementar() {
        return jdbcAmes.query("""
            SELECT * FROM comandes.variacio WHERE NOT processat
            """, (rs, rowNum) -> VariacioBuidaReqImpl.builder()
                                    .liniaComanda(KeyLiniaComanda.of(rs.getLong("comanda"), rs.getLong("numero")))
                                    .datareg(rs.getObject("datareg", LocalDateTime.class))
                                    .articleClient(KeyArticleClient.of(rs.getString("artint"), rs.getString("clicod")))
                                    .divisa(Divisa.getBySymbol(rs.getString("divisa")))
                                    .dataVariacio(rs.getDate("data_variacio").toLocalDate())
                                    .build()
        );
    }

    @Override
    public void complementa(KeyLiniaComanda liniaComanda, LocalDateTime datareg, VariacioComplReq complRecord) {
        String sql = """
                UPDATE comandes.variacio SET
                    client_codi = :client_codi,
                    client_desc = :client_desc,
                    codi_fabrica = :codi_fabrica,
                    referencia = :referencia,
                    fabrica = :fabrica,
                    project_manager = :project_manager,
                    pes = :pes,
                    factor_eur = :factor_eur,
                    valid = :is_valid,
                    processat = true
                WHERE comanda = :comanda AND numero = :numero AND datareg = :datareg
                """;
        Map<String, Object> params = new HashMap<>();
        params.put("client_codi", complRecord.clientCodi());
        params.put("client_desc", complRecord.clientDesc());
        params.put("codi_fabrica", complRecord.codiPesa());
        params.put("referencia", complRecord.referencia());
        params.put("fabrica", complRecord.fabrica());
        params.put("project_manager", complRecord.projectManager());
        params.put("pes", complRecord.pes());
        params.put("factor_eur", complRecord.factEur());
        params.put("is_valid", complRecord.isValid());
        params.put("comanda", liniaComanda.comanda());
        params.put("numero", liniaComanda.numero());
        params.put("datareg", datareg);

        NamedParameterJdbcTemplate namedJdbc = new NamedParameterJdbcTemplate(jdbcAmes);
        namedJdbc.update(sql, params);
    }

    @Override
    public long variacio(KeyArticleClient articleClient, LocalDate dataVariacio, LocalDate dataSolicitadaLimit) {
        var params = new HashMap<String, Object>();
        params.put("artint", articleClient.artint());
        params.put("clicod", articleClient.clicod());
        params.put("dataVariacio", dataVariacio);
        params.put("dataSolicitada", dataSolicitadaLimit);
        var sql = """
                SELECT sum(quantitat)
                FROM comandes.variacio
                WHERE artint = :artint
                    AND clicod = :clicod
                    AND data_variacio = :dataVariacio
                    AND data_solicitada <= :dataSolicitada
                """;
        Long variacio = new NamedParameterJdbcTemplate(jdbcAmes).queryForObject(sql, params, Long.class);
        return variacio != null ? variacio : 0L;
    }

    private void insertIntern(LiniaComanda l, LocalDateTime datareg, LocalDate dataVariacio, boolean isNegatiu) {
        var params = new HashMap<String, Object>();
        params.put("data_variacio", dataVariacio);
        params.put("comanda", l.comanda());
        params.put("numero", l.numero());
        params.put("datareg", datareg);
        params.put("artint", l.articleClient().artint());
        params.put("clicod", l.articleClient().clicod());
        params.put("quantitat", isNegatiu ? -l.quantitat() : l.quantitat());
        params.put("preu", isNegatiu ? l.preu().valor().negate() : l.preu().valor());
        params.put("divisa", l.preu().divisa().symbol());
        params.put("import", isNegatiu ? l.importNet().negate() : l.importNet());
        params.put("data_solicitada", l.dataSolicitada());
        params.put("processat", false);
        params.put("valid", false);
        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("comandes")
                .withTableName("variacio")
                .execute(params);
    }

}

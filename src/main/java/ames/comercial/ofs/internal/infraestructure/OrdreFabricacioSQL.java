package ames.comercial.ofs.internal.infraestructure;

import ames.comercial.ofs.internal.domain.OrdreFabricacio;
import ames.comercial.ofs.internal.domain.OrdreFabricacioImpl;
import ames.comercial.ofs.internal.domain.Termini;
import ames.comercial.ofs.internal.domain.TerminiImpl;
import ames.comercial.server.BatchInsertHelper;
import ames.comercial.server.MapperUtils;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Primary
public class OrdreFabricacioSQL implements OrdreFabricacioRepository {

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

    @Override
    public long nextNumero() {
        return jdbcAmes.queryForObject("SELECT nextval('ofs.seq_ofs')", Long.class);
    }

    @Override
    public Optional<OrdreFabricacio> get(long numero) {
        return new NamedParameterJdbcTemplate(jdbcAmes).query(
                sqlQuery() + " WHERE of.numero = :numero order by t.data asc",
                new MapSqlParameterSource("numero", numero),
                this::mapOrdreFabricacio);
    }

    @Override
    public Optional<OrdreFabricacio> get(KeyArticleClient articleClient) {
        var params = new MapSqlParameterSource("artint", articleClient.artint())
                .addValue("clicod", articleClient.clicod());
        return new NamedParameterJdbcTemplate(jdbcAmes).query(
                sqlQuery() +
                """
                WHERE of.artint = :artint
                    AND of.clicod = :clicod
                    AND (of_posterior IS NULL OR of_posterior = 0)
                order by t.data asc
                """,
                params,
                this::mapOrdreFabricacio);
    }

    private String sqlQuery() {
    return """
            SELECT of.*, t.*, t.quantitat as qtatTermini
            FROM ofs.ordre_fabricacio of
            LEFT JOIN ofs.termini t ON of.numero = t.numero
            """;
    }

    private Optional<OrdreFabricacio> mapOrdreFabricacio(ResultSet rs) throws SQLException {
        // Si no hi ha registres es retorna buit
        if (!rs.next()) return  Optional.empty();

        // Lectura de la capçalera a la primera fila
        var ofBuilder = OrdreFabricacioImpl.builder();
        ofBuilder.numero(rs.getLong("numero"));
        ofBuilder.articleClient(KeyArticleClient.of(rs.getString("artint"),rs.getString("clicod")));
        ofBuilder.fabrica(rs.getString("fabrica"));
        ofBuilder.dataEmissio(rs.getDate("data_emissio").toLocalDate());
        ofBuilder.ofAnterior(MapperUtils.readOptionalLong(rs, "of_anterior"));
        ofBuilder.ofPosterior(MapperUtils.readOptionalLong(rs, "of_posterior"));
        ofBuilder.quantitatRebudaEntrades(rs.getLong("quantitat_rebuda_entrades"));
        ofBuilder.quantitatRebudaEntradesAcumulatTotal(rs.getLong("quantitat_rebuda_entrades_total"));
        ofBuilder.ultimaQuantitatRebudaAbansCreacio(rs.getLong("ultima_quantitat_rebuda_abans_creacio"));
        ofBuilder.dataUltimaQuantitatRebudaAbansCreacio(MapperUtils.readOptionalDate(rs,"data_ultima_quantitat_rebuda_abans_creacio"));
        ofBuilder.ultimaQuantitatRebuda(rs.getLong("ultima_quantitat_rebuda"));
        ofBuilder.dataUltimaQuantitatRebuda(MapperUtils.readOptionalDate(rs,"data_ultima_quantitat_rebuda"));
        ofBuilder.dataAnulacio(MapperUtils.readOptionalDate(rs, "data_anulacio"));
        ofBuilder.diesCalculIncrement(rs.getLong("dies_calcul_increment"));
        ofBuilder.canviFabrica(rs.getBoolean("canvi_fabrica"));
        var lisTerminis = new ArrayList<Termini>();
        do {
            lisTerminis.add(TerminiImpl.builder()
                    .quantitat(rs.getLong("qtatTermini"))
                    .quantitatAnterior(rs.getLong("quantitat_anterior"))
                    .quantitatRebuda(rs.getLong("quantitat_rebuda"))
                    .data(rs.getDate("data").toLocalDate())
                    .dataSortida(rs.getDate("data_sortida").toLocalDate())
                    .isStockSeguretat(rs.getBoolean("is_stock_seguretat"))
                    .build());
        } while (rs.next());
        ofBuilder.terminis(lisTerminis);
        return Optional.of(ofBuilder.build());
    }

    @Override
    public void save(OrdreFabricacio of) {
        // S'eliminen els registres de la of i els seus terminis
        remove(of.numero());
        // INSERT de la of
        var params = new HashMap<String, Object>();
        params.put("numero", of.numero());
        params.put("artint", of.articleClient().artint());
        params.put("clicod", of.articleClient().clicod());
        params.put("fabrica", of.fabrica());
        params.put("data_emissio", of.dataEmissio());
        params.put("of_anterior", of.ofAnterior().orElse(null));
        params.put("of_posterior", of.ofPosterior().orElse(null));
        params.put("data_anulacio", of.dataAnulacio().orElse(null));
        params.put("quantitat", of.quantitatTotal());
        params.put("quantitat_rebuda_entrades", of.quantitatRebudaEntrades());
        params.put("quantitat_rebuda_terminis", of.quantitatRebudaTerminis());
        params.put("quantitat_pendent", of.quantitatPendent());
        params.put("quantitat_exces", of.exces());
        params.put("quantitat_rebuda_entrades_total", of.quantitatRebudaEntradesAcumulatTotal());
        params.put("ultima_quantitat_rebuda_abans_creacio", of.ultimaQuantitatRebudaAbansCreacio());
        params.put("data_ultima_quantitat_rebuda_abans_creacio", of.dataUltimaQuantitatRebudaAbansCreacio().orElse(null));
        params.put("ultima_quantitat_rebuda", of.ultimaQuantitatRebuda());
        params.put("data_ultima_quantitat_rebuda", of.dataUltimaQuantitatRebuda().orElse(null));
        params.put("dies_calcul_increment", of.diesCalculIncrement());
        params.put("existeix_increment", of.existeixIncrement());
        params.put("canvi_fabrica", of.canviFabrica());
        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("ofs")
                .withTableName("ordre_fabricacio")
                .execute(params);
        // INSERT dels terminis
        saveTerminis(of.numero(), of.terminis());
    }

    private void remove (long numero) {
        jdbcAmes.update("DELETE FROM ofs.ordre_fabricacio WHERE numero = ?", numero);
        jdbcAmes.update("DELETE FROM ofs.termini WHERE numero = ?", numero);
    }

    private void saveTerminis(long numero, List<Termini> terminis) {
        var registres = new ArrayList<Map<String, Object>>();
        terminis.forEach(t -> {
            var params = new HashMap<String, Object>();
            params.put("numero", numero);
            params.put("data", t.data());
            params.put("data_sortida", t.dataSortida());
            params.put("quantitat_anterior", t.quantitatAnterior());
            params.put("quantitat", t.quantitat());
            params.put("quantitat_rebuda", t.quantitatRebuda());
            params.put("is_stock_seguretat", t.isStockSeguretat());
            registres.add(params);
        });
        new BatchInsertHelper(jdbcAmes)
                .withSchemaName("ofs")
                .withTableName("termini")
                .execute(registres);
    }

}

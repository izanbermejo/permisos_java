package ames.comercial.inventari.internal.infraestructure.fitxa;

import ames.comercial.advantage.internal.auxiliar.ConditionGenerator;
import ames.comercial.inventari.internal.domain.fitxa.Fitxa;
import ames.comercial.inventari.internal.domain.fitxa.KeyFitxa;
import ames.comercial.inventari.internal.infraestructure.fitxa.mapper.FitxaMapper;
import ames.comercial.shared.KeyArticleClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class FitxaRepositorySQL implements FitxaRepository {

    @Autowired @Qualifier("jdbcAmes") JdbcTemplate jdbcAmes;

    @Override
    public void save(Fitxa fitxa) {
        var params = new HashMap<String, Object>();
        params.put("artint", fitxa.id().articleClient().artint());
        params.put("clicod", fitxa.id().articleClient().clicod());
        params.put("empresa", fitxa.id().empresa());
        params.put("magatzem", fitxa.id().magatzem());
        params.put("stock", fitxa.stock());
        params.put("stock_reservat", fitxa.stockReservat());
        params.put("actiu", fitxa.isActiu());
        params.put("data_creacio", fitxa.dataCreacio());

        new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("inventari")
                .withTableName("fitxa")
                .execute(params);
    }

    /**
     * TODO: Eliminar després de la migració
     */
    public void saveBatch(List<Fitxa> fitxes) {
        if (fitxes.isEmpty()) {
            return;
        }
        var insertBatch = new SimpleJdbcInsert(jdbcAmes)
                .withSchemaName("inventari")
                .withTableName("fitxa");
        // Processar en lotes per evitar problemes de memòria
        for (int i = 0; i < fitxes.size(); i += 2000) {
            int end = Math.min(i + 2000, fitxes.size());
            var subList = fitxes.subList(i, end);
            var batchParams = subList.stream()
                    .map(f -> {
                        var params = new HashMap<String, Object>();
                        params.put("artint", f.id().articleClient().artint());
                        params.put("clicod", f.id().articleClient().clicod());
                        params.put("empresa", f.id().empresa());
                        params.put("magatzem", f.id().magatzem());
                        params.put("stock", f.stock());
                        params.put("stock_reservat", f.stockReservat());
                        params.put("actiu", f.isActiu());
                        params.put("data_creacio", f.dataCreacio());
                        return params;
                    })
                    .toArray(HashMap[]::new);
            insertBatch.executeBatch(batchParams);
        }
    }

    @Override
    public List<Fitxa> find(KeyArticleClient articleClient) {
        return jdbcAmes.query(
                "SELECT * FROM inventari.fitxa WHERE artint = ? AND clicod = ?",
                new FitxaMapper(),
                articleClient.artint(),
                articleClient.clicod()
        );
    }

    @Override
    public List<Fitxa> find(Set<KeyArticleClient> articleClient, String empresa, String magatzem) {
        List<Object> params = new ArrayList<>();
        params.add(magatzem);
        params.add(empresa);
        articleClient.forEach(ac -> {
            params.add(ac.artint());
            params.add(ac.clicod());
        });
        return jdbcAmes.query(
                String.format("""
                SELECT *
                FROM inventari.fitxa
                WHERE magatzem = ? AND empresa = ?
                    AND (artint, clicod) IN (%s)
                """, ConditionGenerator.generate(articleClient, 2)),
                new FitxaMapper(),
                params.toArray()
        );
    }

    @Override
    public Optional<Fitxa> find(KeyFitxa id) {
        try {
            return Optional.ofNullable(jdbcAmes.queryForObject(
                    "SELECT * FROM inventari.fitxa WHERE artint = ? AND clicod = ? AND empresa = ? AND magatzem = ?",
                    new FitxaMapper(),
                    id.articleClient().artint(),
                    id.articleClient().clicod(),
                    id.empresa(),
                    id.magatzem()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateStock(KeyFitxa id, long incrementStock, long incrementStockReservat) {
        var params = new MapSqlParameterSource()
                .addValue("incrementStock", incrementStock)
                .addValue("incrementStockReservat", incrementStockReservat)
                .addValue("artint", id.articleClient().artint())
                .addValue("clicod", id.articleClient().clicod())
                .addValue("empresa", id.empresa())
                .addValue("magatzem", id.magatzem());

        new NamedParameterJdbcTemplate(jdbcAmes).update(
                """
                   UPDATE inventari.fitxa
                   SET stock = stock + :incrementStock,
                          stock_reservat = stock_reservat + :incrementStockReservat
                   WHERE artint = :artint AND clicod = :clicod AND empresa = :empresa AND magatzem = :magatzem
                 """,
                params
        );
    }

    @Override
    public long updateStockReservat(KeyFitxa id, long stockReservat) {
        var params = new MapSqlParameterSource()
                .addValue("stockReservat", stockReservat)
                .addValue("artint", id.articleClient().artint())
                .addValue("clicod", id.articleClient().clicod())
                .addValue("empresa", id.empresa())
                .addValue("magatzem", id.magatzem());

        return new NamedParameterJdbcTemplate(jdbcAmes).update(
                """
                    UPDATE inventari.fitxa
                    SET stock_reservat = :stockReservat
                    WHERE
                        artint = :artint
                        AND clicod = :clicod
                        AND empresa = :empresa
                        AND magatzem = :magatzem
                    AND :stockReservat <= stock
                """,
                params
        );
    }

    @Override
    public long incrementaStockReservat(KeyFitxa id, long incrementStockReservat) {
        var params = new MapSqlParameterSource()
                .addValue("incrementStockReservat", incrementStockReservat)
                .addValue("artint", id.articleClient().artint())
                .addValue("clicod", id.articleClient().clicod())
                .addValue("empresa", id.empresa())
                .addValue("magatzem", id.magatzem());

        return new NamedParameterJdbcTemplate(jdbcAmes).update(
                """
                    UPDATE inventari.fitxa
                    SET stock_reservat = stock_reservat + :incrementStockReservat
                    WHERE
                        artint = :artint
                        AND clicod = :clicod
                        AND empresa = :empresa
                        AND magatzem = :magatzem
                    AND stock_reservat + :incrementStockReservat <= stock
                """,
                params
        );
    }

}

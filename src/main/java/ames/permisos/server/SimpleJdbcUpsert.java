package ames.permisos.server;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleJdbcUpsert {

    private final JdbcTemplate jdbcTemplate;
    private String schema;
    private String table;
    private String[] conflictColumns;

    public SimpleJdbcUpsert(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public SimpleJdbcUpsert withSchemaName(String schema) {
        this.schema = schema;
        return this;
    }

    public SimpleJdbcUpsert withTableName(String table) {
        this.table = table;
        return this;
    }

    public SimpleJdbcUpsert onConflictColumns(String... conflictColumns) {
        this.conflictColumns = conflictColumns;
        return this;
    }

    public int execute(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("S'han de definir quins son els valors per fer l'UPSERT");
        }
        if (conflictColumns == null || conflictColumns.length == 0) {
            throw new IllegalArgumentException("S'han de definir quines son les columnes de conflicte per fer l'UPSERT");
        }

        // Nom de la taula amb esquema
        String fullTable = (schema != null ? schema + "." : "") + table;

        String[] columns = values.keySet().toArray(new String[0]);
        // Columnes d'INSERT
        String insertCols = String.join(", ", columns);
        String insertParams = Arrays.stream(columns).map(c -> "?").collect(Collectors.joining(", "));

        // Columnes d'UPDATE (Totes menys les de conflicte)
        String[] updateCols = Arrays.stream(columns)
                .filter(c -> Arrays.stream(conflictColumns).noneMatch(cc -> cc.equals(c)))
                .toArray(String[]::new);

        String updateClause = Arrays.stream(updateCols)
                .map(c -> c + " = EXCLUDED." + c)
                .collect(Collectors.joining(", "));

        String conflictClause = String.join(", ", conflictColumns);

        String sql;

        if (updateCols.length == 0) {
            sql = String.format("""
                INSERT INTO %s (%s)
                VALUES (%s)
                ON CONFLICT (%s)
                DO NOTHING
                """,
            fullTable, insertCols, insertParams, conflictClause);
        } else {
            sql = String.format("""
                INSERT INTO %s (%s)
                VALUES (%s)
                ON CONFLICT (%s)
                DO UPDATE SET %s
                """,
            fullTable, insertCols, insertParams, conflictClause, updateClause);
        }

        Object[] params = values.values().toArray();

        int[] types = buildTypes(values);

        return jdbcTemplate.update(sql, params, types);
    }

    private int[] buildTypes(Map<String, Object> values) {
        SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
                .withSchemaName(schema)
                .withTableName(table)
                .usingColumns(values.keySet().toArray(new String[0]));
        insert.compile();
        return insert.getInsertTypes();
    }

}

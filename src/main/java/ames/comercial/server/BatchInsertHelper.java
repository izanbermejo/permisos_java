package ames.comercial.server;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class BatchInsertHelper {

    private final JdbcTemplate jdbc;

    private String schema;
    private String table;

    public BatchInsertHelper(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public BatchInsertHelper withSchemaName(String schema) {
        this.schema = schema;
        return this;
    }

    public BatchInsertHelper withTableName(String table) {
        this.table = table;
        return this;
    }

    public void execute(List<Map<String, Object>> registres) {
        if (registres.isEmpty())
            return;

        if (schema == null || table == null) {
            throw new IllegalStateException("S'ha d'especificar la taula i l'esquema");
        }

        // Obtenció dels noms de les columnes
        Set<String> columns = registres.get(0).keySet();
        // Nom de les columnes separat per comes
        String columnNames = String.join(", ", columns);
        // Paràmetres (?) separats per comes
        String namedParams = columns.stream()
                .map(col -> "?")
                .collect(Collectors.joining(", "));

        String sql = String.format("INSERT INTO %s.%s (%s) VALUES (%s)", schema, table, columnNames, namedParams);

        List<Object[]> batchParams = registres.stream()
                .map(r -> columns.stream()
                        .map(r::get)
                        .toArray())
                .collect(Collectors.toList());

        jdbc.batchUpdate(sql, batchParams);
    }

}

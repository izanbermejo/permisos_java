package ames.comercial.advantage.internal.auxiliar.statements;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MultipleInsertStatement<T> {

	private Connection connection;
	private String tableName;
	private Map<String, Function<T, Object>> mapColumnsValues = new HashMap<String, Function<T, Object>>();
	
	public MultipleInsertStatement (Connection connection, String tableName) {
		this.connection = connection;
		this.tableName = tableName;
	}
	
	public MultipleInsertStatement<T> add(String column, Function<T, Object> value) {
		if (value != null)
			mapColumnsValues.put(column, value);
		return this;
	}

	public PreparedStatement build(Set<T> values) throws SQLException {
		return build(values.stream().toList());
	}

	public PreparedStatement build(List<T> values) throws SQLException {
		var statement = connection.prepareStatement(generateQuery());
		for (var v : values) {
			int i = 1;
			for (var arg : mapColumnsValues.values()) {
				var res = arg.apply(v);
				if (res == null) {
					statement.setNull(i++, java.sql.Types.VARCHAR);
				} else if (res instanceof LocalDate) {
					statement.setDate(i++, Date.valueOf((LocalDate)res));
			    } else if (res instanceof Integer) {
			    	statement.setInt(i++, (Integer) res);
			    } else if (res instanceof Long) {
			    	statement.setLong(i++, (Long) res);
			    } else if (res instanceof Double) {
			    	statement.setDouble(i++, (Double) res);
			    } else if (res instanceof Float) {
			    	statement.setFloat(i++, (Float) res);
			    } else if (res instanceof String) {
			    	statement.setString(i++, (String) res);
			    } else {
			    	statement.setObject(i++, res);
			    }
			}
			statement.addBatch();
		}
		return statement;
	}
	
	private String generateQuery() {
		String sql = "INSERT INTO " + tableName + " (";
		String params = " VALUES (";
		for (var col : mapColumnsValues.keySet()) {
			sql += col + ",";
			params += "?,";
		}
		return sql.substring(0, sql.length()-1) + ")" 
				+ params.substring(0, params.length()-1) + ")";
	}
	
}

package ames.comercial.advantage.internal.auxiliar.statements;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class InsertStatement {

	private Connection connection;
	private String tableName;
	private Map<String, Object> mapColumnsValues = new HashMap<String, Object>();
	
	public InsertStatement (Connection connection, String tableName) {
		this.connection = connection;
		this.tableName = tableName;
	}
	
	public InsertStatement add(String column, Object value) {
		if (value != null)
			mapColumnsValues.put(column, value);
		return this;
	}
	
	public PreparedStatement build() throws SQLException {
		var statement = connection.prepareStatement(generateQuery());
		int i = 1;
		for (var arg : mapColumnsValues.values()) {
			if (arg instanceof LocalDate) {
				statement.setDate(i++, Date.valueOf((LocalDate)arg));
		    } else if (arg instanceof Integer) {
		    	statement.setInt(i++, (Integer) arg);
		    } else if (arg instanceof Long) {
		    	statement.setLong(i++, (Long) arg);
		    } else if (arg instanceof Double) {
		    	statement.setDouble(i++, (Double) arg);
		    } else if (arg instanceof Float) {
		    	statement.setFloat(i++, (Float) arg);
		    } else if (arg instanceof String) {
		    	statement.setString(i++, (String) arg);
		    } else {
		    	statement.setObject(i++, arg);
		    }
		}
		statement.addBatch();
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

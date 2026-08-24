package ames.comercial.advantage.internal;

import ames.comercial.advantage.AdvantageConfig;
import ames.comercial.server.BeanUtils;
import ames.comercial.server.exception.AppException;
import com.extendedsystems.jdbc.advantage.ADSException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.List;

public class AdvantageDao {

	static final Logger log = LogManager.getLogger(AdvantageDao.class.getName());

	static String databasePath;
	static String databaseMagPath;

	public AdvantageDao() {
		databasePath = BeanUtils.getBean(AdvantageConfig.class).getPath();
		databaseMagPath = BeanUtils.getBean(AdvantageConfig.class).getPathmag();
	}

	public interface ConnectionProvider {
		Connection get() throws ClassNotFoundException, SQLException;
	}

	public interface PreparedStatementProvider {
		PreparedStatement get(Connection connection) throws SQLException;
	}

	public interface PreparedStatementProviderBatch extends PreparedStatementProvider {
		PreparedStatement get(Connection connection) throws SQLException;
	}

	public interface ResultSetAction<T> {
		T get(ResultSet rs) throws SQLException;
	}

	private final ConnectionProvider connectionCom = () -> {
		Class.forName("com.extendedsystems.jdbc.advantage.ADSDriver");
		return DriverManager.getConnection(String.format("jdbc:p6spy:extendedsystems:advantage:%s;password=;showDeleted=False;", databasePath));
	};

	public static final ConnectionProvider connectionMag = () -> {
		Class.forName("com.extendedsystems.jdbc.advantage.ADSDriver");
		return DriverManager.getConnection(String.format("jdbc:p6spy:extendedsystems:advantage:%s;password=;showDeleted=False;", databaseMagPath));
	};

	public static String queryOrder (String query, String colName, boolean asc) {
		if (colName == null || colName.isBlank())
			return query;
		return query + " ORDER BY " + colName + (asc ? " ASC" : " DESC");
	}

	public <T> T query(PreparedStatementProvider preparedStatementProvider, ResultSetAction<T> preparedStatementAction) {
		return query (connectionCom, preparedStatementProvider, preparedStatementAction);
	}

	public <T> T query(ConnectionProvider connectionProvider, PreparedStatementProvider preparedStatementProvider, ResultSetAction<T> preparedStatementAction) {
		Connection connection = null;
		ResultSet rs = null;
		PreparedStatement statement = null;
		try {
			connection = connectionProvider.get();
			statement = preparedStatementProvider.get(connection);
			rs = statement.executeQuery();
			return preparedStatementAction.get(rs);
		} catch (ADSException e) {
			if (e.getErrorCode() == 5401) {
				throw new AppException("Error 5401 de connexió a l'Advantage");
			}
			throw new RuntimeException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			closeConnection(connection, rs, statement);
		}
	}

	public void executeUpdate(List<PreparedStatementProvider> statements) {
		executeUpdate(statements.toArray(new PreparedStatementProvider[0]));
	}

	public void executeUpdate(ConnectionProvider connectionProvider, List<PreparedStatementProvider> statements) {
		executeUpdate(connectionProvider, statements.toArray(new PreparedStatementProvider[0]));
	}

	public void executeUpdate(PreparedStatementProvider... preparedStatementProvider) {
		executeUpdate(connectionCom, preparedStatementProvider);
	}

	public void executeUpdate(ConnectionProvider connectionProvider, PreparedStatementProvider... preparedStatementProvider) {
		Connection connection = null;
		try {
			connection = connectionProvider.get();
			connection.setAutoCommit(false);
			for(PreparedStatementProvider statementProv : preparedStatementProvider) {
				var statement = statementProv.get(connection);
				if (statementProv instanceof PreparedStatementProviderBatch) {
					statement.executeBatch();
				} else {
					statement.executeUpdate();
				}
			}
			connection.commit();
		} catch (Exception e) {
			log.error("EXCEPCIÓ ADVANTAGE: ", e);
			rollback(connection);
			throw new RuntimeException("Error Advantage");
		} finally {
			closeConnection(connection);
		}
	}

	private void rollback(Connection connection) {
		if (connection != null) {
			try {
				connection.rollback();
			} catch (SQLException e) {
				log.error("EXCEPCIÓ FENT ROLLBACK: ", e);
			}
		}
	}

	private void closeConnection(Connection connection) {
		closeConnection(connection, null);
	}

	private void closeConnection(Connection connection, Statement statement) {
		closeConnection(connection, null, statement);
	}

	private void closeConnection(Connection connection, ResultSet resultSet, Statement statement) {
		if (connection != null) {
			try {
				connection.close();
			} catch (Exception e) {}
		}
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (Exception e) {}
		}
		if (statement != null) {
			try {
				statement.close();
			} catch (Exception e) {}
		}
	}

}
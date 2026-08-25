package ames.permisos.server;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class MapperUtils {

	public static Optional<LocalDate> readOptionalDate(ResultSet rs, String col) throws SQLException {
		var date = rs.getDate(col);
		return (date != null && date.toLocalDate().isAfter(LocalDate.of(1900, 1, 1)))
				? Optional.of(date.toLocalDate())
				: Optional.empty();
	}

	public static Optional<Long> readOptionalLong(ResultSet rs, String col) throws SQLException {
		long value = rs.getLong(col);
		return rs.wasNull() ? Optional.empty() : Optional.of(value);
	}

	public static Optional<BigDecimal> readOptionalBigdecimal(ResultSet rs, String col) throws SQLException {
		var value = rs.getBigDecimal(col);
		return rs.wasNull() ? Optional.empty() : Optional.of(value);
	}

	public static Optional<String> readOptionalString(ResultSet rs, String col) throws SQLException {
		var value = rs.getString(col);
		return rs.wasNull() ? Optional.empty() : Optional.of(value);
	}

	public static Optional<LocalDateTime> readOptionalLocalDateTime(ResultSet rs, String col) throws SQLException {
		var value = rs.getTimestamp(col);
		return rs.wasNull() ? Optional.empty() : Optional.of(value.toLocalDateTime());
	}
	
	public static String readSafe(ResultSet rs, String col) throws SQLException {
		var s = rs.getString(col);
		return s != null ? s : "";
	}

	public static Optional<BigDecimal> readOptionalBigDecimal(ResultSet rs, String col) throws SQLException{
		BigDecimal value = rs.getBigDecimal(col);
		return Optional.ofNullable(value);
	}
	
}

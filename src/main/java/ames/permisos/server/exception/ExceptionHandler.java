package ames.permisos.server.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;

@Provider
public class ExceptionHandler implements ExceptionMapper<Throwable> {

	static final Logger log = LogManager.getLogger(ExceptionHandler.class.getName());

	private @Autowired ObjectMapper jsonMapper;
	
	private record JsonException (
		@JsonProperty("httpCode") int httpCode, 
		@JsonProperty("message") String message, 
		@JsonProperty("stackTraceError") String stackTraceError
	) {}
	 
	@Override
	public Response toResponse (Throwable exception) {
		log.error(exception.getMessage());
		log.error("EXCEPTION: ", exception);
		
		int httpCode = 500;
		if (exception instanceof WebApplicationException webException)
			httpCode = webException.getResponse().getStatus();
		if (exception instanceof AppException appException)
			httpCode = appException.httpCode();
		var error = new JsonException(httpCode, exception.getMessage(), stackTraceToString(exception));
		return Response.status(httpCode).entity(toJson(error)).type(MediaType.APPLICATION_JSON).build();
	}

	private String toJson (JsonException error) {
		try {
			return jsonMapper.writeValueAsString(error);
		} catch (Exception e) {
			return error.stackTraceError;
		}
	}
	
	/** Retorna la pila d'errors d'un Exception en un String */
	private String stackTraceToString (Throwable exception) {
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		try {
			exception.printStackTrace(printWriter);
			return stringWriter.toString();
		} catch (Exception error) {
			return "stackTraceToString error. " + error.getMessage();
		} finally {
			if (stringWriter != null)
				try { stringWriter.close(); } catch (Exception ignored) {};
			printWriter.close();
		}
	}	
}
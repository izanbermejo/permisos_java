package ames.permisos.server.filters;

import ames.permisos.server.Json;
import ames.permisos.server.Languages;
import ames.permisos.server.RequestThread;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Provider
public class RequestFilter implements ContainerRequestFilter {

	@Context private HttpServletRequest httpRequest;
	@Autowired private ObjectMapper jsonMapper;
	private Json json;
	private TypeReference<Map<RequestThread.Entity,String>> mapType;
	private final DateTimeFormatter dataLocalFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	@PostConstruct
	public void init () {
		json = new Json(jsonMapper);
		this.mapType = new TypeReference<Map<RequestThread.Entity,String>>() {};
	}
	
	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
	  	// Adjunta al thread de la request l'idioma esperat pel client, l'usuari, email, l'adreça IP remota i l'etag
	  	var etagHeader = ctx.getHeaderString("etag");
	  	Map<RequestThread.Entity,String> etags = (etagHeader == null) ? Map.of() : json.deserialize(etagHeader, mapType);

		// Data local
		var dataLocalStr = ctx.getHeaderString("DataLocal");
		var dataLocal = LocalDateTime.now();
		try {
			dataLocal = LocalDateTime.parse(dataLocalStr, dataLocalFormatter);
		} catch (Exception e) { }

		RequestThread.set(Languages.getPriorAccepted(ctx.getAcceptableLanguages()), 
				httpRequest.getRemoteAddr(),
				ctx.getHeaderString("usuari"),
				ctx.getHeaderString("nom_usuari"),
				ctx.getHeaderString("email"),
				dataLocal,
				etags);

		// Set de l'MDC per als logs
		MDC.put("userId", RequestThread.codiUsuari());
		MDC.put("ip", RequestThread.ipAddress());
		MDC.put("user", RequestThread.nomUsuari());
	}
  
}

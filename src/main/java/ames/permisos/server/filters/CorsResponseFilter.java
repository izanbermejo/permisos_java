package ames.permisos.server.filters;

import org.springframework.context.annotation.Profile;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * Filtre de resposta per habilitar CORS. Per ara es permeten peticions de tots els origens.
 */
@Provider
@PreMatching
@Profile("test")
public class CorsResponseFilter implements ContainerResponseFilter {

	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		MultivaluedMap<String, Object> headers = responseContext.getHeaders();
		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Accept", "application/json");
		headers.add("Access-Control-Allow-Credentials", "true");
		headers.add("Access-Control-Allow-Methods", "OPTIONS, GET, POST, DELETE, PUT");
		headers.add("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Origin, Access-Control-Allow-Credentials, Access-Control-Allow-Methods, Access-Control-Allow-Headers, Authorization, X-Requested-With, Accept-Charset, Accept-Language, params, nom_usuari, usuari, etag,email, datalocal");
	}
	
}

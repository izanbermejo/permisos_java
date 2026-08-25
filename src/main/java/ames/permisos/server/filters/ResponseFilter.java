package ames.permisos.server.filters;

import ames.permisos.server.RequestThread;
import org.slf4j.MDC;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class ResponseFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestCtx, ContainerResponseContext responseCtx) throws IOException {
		// Neteja del Thread amb dades de la request
		RequestThread.clean();
		// Neteja MDC per logs
		MDC.clear();
	}
	
}

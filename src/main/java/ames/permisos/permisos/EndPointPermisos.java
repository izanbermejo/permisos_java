package ames.permisos.permisos;

import ames.permisos.permisos.internal.application.query.ObtenirPermisosFuncio;
import ames.permisos.permisos.internal.application.query.ObtenirPermisosFuncio.ObtenirPermisosFuncioResponse;
import ames.permisos.server.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/tarifes")
public class EndPointPermisos {

    static final Logger log = LogManager.getLogger(EndPointPermisos.class.getName());

    @GET
    @Path("/permisosFuncio")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ObtenirPermisosFuncioResponse> obtenirPermisosFuncio() {
        return BeanUtils.getBean(ObtenirPermisosFuncio.class).executar();
    }
}
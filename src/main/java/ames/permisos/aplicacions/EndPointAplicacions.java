package ames.permisos.aplicacions;

import ames.permisos.aplicacions.internal.application.command.ModificarAplicacio;
import ames.permisos.aplicacions.internal.application.command.CrearAplicacio;
import ames.permisos.aplicacions.internal.application.command.EliminarAplicacio;
import ames.permisos.aplicacions.internal.application.query.ObtenirAplicacioByNom;
import ames.permisos.aplicacions.internal.application.query.ObtenirAplicacions;
import ames.permisos.aplicacions.internal.application.query.ObtenirAplicacions.ObtenirAplicacionsResponse;
import ames.permisos.aplicacions.internal.domain.Aplicacio;
import ames.permisos.server.BeanUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;

@Path("/aplicacions")
public class EndPointAplicacions {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<ObtenirAplicacionsResponse> obtenirAplicacions() {
        return BeanUtils.getBean(ObtenirAplicacions.class).executar();
    }

    @GET
    @Path("{nomAplicacio}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<Aplicacio> obtenirAplicacioByNom(@PathParam("nomAplicacio") String nomAplicacio) {
        return BeanUtils.getBean(ObtenirAplicacioByNom.class).executar(nomAplicacio);
    }

    @DELETE
    @Path("delete/{nomAplicacio}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void eliminarAplicacio(@PathParam("nomAplicacio") String nomAplicacio) {
        BeanUtils.getBean(EliminarAplicacio.class).executar(nomAplicacio);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void crearAplicacio(Aplicacio aplicacio) {
        BeanUtils.getBean(CrearAplicacio.class).executar(aplicacio);
    }

    @PUT
    @Path("{nomAplicacio}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void modificarAplicacio(@PathParam("nomAplicacio") String nomAplicacio, Aplicacio aplicacio) {
        BeanUtils.getBean(ModificarAplicacio.class).executar(nomAplicacio, aplicacio.descripcio());
    }
}
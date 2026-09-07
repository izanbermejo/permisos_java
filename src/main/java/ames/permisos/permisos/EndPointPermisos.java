package ames.permisos.permisos;

import ames.permisos.organigrama.internal.domain.Empleat;
import ames.permisos.organigrama.internal.domain.Funcio;
import ames.permisos.permisos.internal.application.command.*;
import ames.permisos.permisos.internal.application.query.*;
import ames.permisos.permisos.internal.domain.Permis;
import ames.permisos.server.BeanUtils;
import ames.permisos.shared.ResultatEliminacio;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Path("/permisos")
public class EndPointPermisos {

    @GET
    @Path("{nomAplicacio}/moduls")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Permis> obtenirPermisosByModul(@PathParam("nomAplicacio") String nomAplicacio,
                                               @QueryParam("nomModuls") List<String> request) {
        return BeanUtils.getBean(ObtenirPermisosByModuls.class).executar(nomAplicacio, request);
    }

    @GET
    @Path("{nomAplicacio}/{nomModul}/{nomPermis}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<Permis> obtenirPermisByNom(@PathParam("nomAplicacio") String nomAplicacio,
                                               @PathParam("nomModul") String nomModul,
                                               @PathParam("nomPermis") String nomPermis) {
        return BeanUtils.getBean(ObtenirPermisByNom.class).executar(nomAplicacio, nomModul, nomPermis);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void crearPermis(Permis permis) {
        BeanUtils.getBean(CrearPermis.class).executar(permis);
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void modificarPermis(Permis permis) {
        BeanUtils.getBean(ModificarPermis.class).executar(permis);
    }

    @DELETE
    @Path("delete/{nomAplicacio}/{nomModul}/{nomPermis}")
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultatEliminacio eliminarPermis(@PathParam("nomAplicacio") String nomAplicacio,
                                             @PathParam("nomModul") String nomModul,
                                             @PathParam("nomPermis") String nomPermis,
                                             @QueryParam("confirmar") boolean confirmar) {
        return BeanUtils.getBean(EliminarPermis.class).executar(nomAplicacio,nomModul, nomPermis, confirmar);
    }

    @GET
    @Path("{nomAplicacio}/{nomModul}/{nomPermis}/funcions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Funcio> obtenirFuncionsByPermis(@PathParam("nomAplicacio") String nomAplicacio,
                                               @PathParam("nomModul") String nomModul,
                                               @PathParam("nomPermis") String nomPermis) {
        return BeanUtils.getBean(ObtenirFuncionsDelPermis.class).executar(nomAplicacio, nomModul, nomPermis);
    }

    @DELETE
    @Path("funcio/delete/{nomAplicacio}/{nomModul}/{nomPermis}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void eliminarFuncioDelPermis(@PathParam("nomAplicacio") String nomAplicacio,
                                       @PathParam("nomModul") String nomModul,
                                       @PathParam("nomPermis") String nomPermis,
                                       Funcio funcio) {
        BeanUtils.getBean(EliminarFuncioDelPermis.class).executar(nomAplicacio, nomModul, nomPermis, funcio);
    }

    @POST
    @Path("funcio/{nomAplicacio}/{nomModul}/{nomPermis}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void assignarFuncio(@PathParam("nomAplicacio") String nomAplicacio,
                               @PathParam("nomModul") String nomModul,
                               @PathParam("nomPermis") String nomPermis,
                               Funcio funcio) {
        BeanUtils.getBean(AssignarFuncioPermis.class).executar(nomAplicacio, nomModul, nomPermis, funcio);
    }

    @GET
    @Path("{nomAplicacio}/{nomModul}/{nomPermis}/empleats")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Empleat> obtenirEmpleatsByPermis(@PathParam("nomAplicacio") String nomAplicacio,
                                                @PathParam("nomModul") String nomModul,
                                                @PathParam("nomPermis") String nomPermis) {
        return BeanUtils.getBean(ObtenirEmpleatsDelPermis.class).executar(nomAplicacio, nomModul, nomPermis);
    }

    @POST
    @Path("empleat/{nomAplicacio}/{nomModul}/{nomPermis}/{idEmpleat}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void assignarEmpleat(@PathParam("nomAplicacio") String nomAplicacio,
                                @PathParam("nomModul") String nomModul,
                                @PathParam("nomPermis") String nomPermis,
                                @PathParam("idEmpleat") int idEmpleat) {
        BeanUtils.getBean(AssignarEmpleatPermis.class).executar(nomAplicacio, nomModul, nomPermis, idEmpleat);
    }

    @DELETE
    @Path("empleat/delete/{nomAplicacio}/{nomModul}/{nomPermis}/{idEmpleat}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void eliminarEmpleatDelModul(@PathParam("nomAplicacio") String nomAplicacio,
                                        @PathParam("nomModul") String nomModul,
                                        @PathParam("nomPermis") String nomPermis,
                                        @PathParam("idEmpleat") int idEmpleat) {
        BeanUtils.getBean(EliminarEmpleatDelPermis.class).executar(nomAplicacio, nomModul, nomPermis, idEmpleat);
    }

    @GET
    @Path("{nomAplicacio}/{nomModul}/{nomPermis}/empleats/tots")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Empleat> obtenirTotsElsEmpleatsByModul(@PathParam("nomAplicacio") String nomAplicacio,
                                                       @PathParam("nomModul") String nomModul,
                                                       @PathParam("nomPermis") String nomPermis) {
        return BeanUtils.getBean(ObtenirTotsElsEmpleatsDelPermis.class).executar(nomAplicacio, nomModul, nomPermis);
    }
}
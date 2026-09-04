package ames.permisos.moduls;

import ames.permisos.moduls.internal.application.command.*;
import ames.permisos.moduls.internal.application.query.*;
import ames.permisos.moduls.internal.domain.Modul;
import ames.permisos.organigrama.internal.domain.Empleat;
import ames.permisos.organigrama.internal.domain.Funcio;
import ames.permisos.server.BeanUtils;
import ames.permisos.shared.ResultatEliminacio;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Path("/moduls")
public class EndPointModuls {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Modul> obtenirModulso() {
        return BeanUtils.getBean(ObtenirModuls.class).executar();
    }

    @GET
    @Path("aplicacions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Modul> obtenirModulsByAplicacio(@QueryParam("nomAplicacions") List<String> request) {
        return BeanUtils.getBean(ObtenirModulsByAplicacio.class).executar(request);
    }

    @GET
    @Path("{nomAplicacio}/{nomModul}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<Modul> obtenirModulByNom(@PathParam("nomAplicacio") String nomAplicacio,
                                             @PathParam("nomModul") String nomModul) {
        return BeanUtils.getBean(ObtenirModulByNom.class).executar(nomAplicacio, nomModul);
    }

    @DELETE
    @Path("delete/{nomAplicacio}/{nomModul}")
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultatEliminacio eliminarModul(@PathParam("nomAplicacio") String nomAplicacio,
                                            @PathParam("nomModul") String nomModul,
                                            @QueryParam("confirmar") boolean confirmar) {
        return BeanUtils.getBean(EliminarModul.class).executar(nomAplicacio, nomModul, confirmar);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void crearModul(Modul modul) {
        BeanUtils.getBean(CrearModul.class).executar(modul);
    }

    @PUT
    @Path("{nomAplicacio}/{nomModul}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void modificarModul(@PathParam("nomAplicacio") String nomAplicacio,
                               @PathParam("nomModul") String nomModul,
                               Modul modul) {
        BeanUtils.getBean(ModificarModul.class).executar(nomAplicacio, nomModul, modul.descripcio());
    }

    @GET
    @Path("funcions/{nomAplicacio}/{nomModul}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Funcio> obtenirFuncionsByModul(@PathParam("nomAplicacio") String nomAplicacio,
                                                    @PathParam("nomModul") String nomModul) {
        return BeanUtils.getBean(ObtenirFuncionsDelModul.class).executar(nomAplicacio, nomModul);
    }

    @POST
    @Path("funcio/{nomAplicacio}/{nomModul}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void assignarFuncio(@PathParam("nomAplicacio") String nomAplicacio,
                               @PathParam("nomModul") String nomModul,
                               Funcio funcio) {
        BeanUtils.getBean(AssignarFuncioModul.class).executar(nomAplicacio, nomModul, funcio);
    }

    @DELETE
    @Path("funcio/delete/{nomAplicacio}/{nomModul}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void eliminarFuncioDelModul(@PathParam("nomAplicacio") String nomAplicacio,
                                       @PathParam("nomModul") String nomModul,
                                       Funcio funcio) {
        BeanUtils.getBean(EliminarFuncioDelModul.class).executar(nomAplicacio, nomModul, funcio);
    }

    @GET
    @Path("empleats/{nomAplicacio}/{nomModul}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Empleat> obtenirEmpleatsByModul(@PathParam("nomAplicacio") String nomAplicacio,
                                                @PathParam("nomModul") String nomModul) {
        return BeanUtils.getBean(ObtenirEmpleatsDelModul.class).executar(nomAplicacio, nomModul);
    }

    @POST
    @Path("empleat/{nomAplicacio}/{nomModul}/{idEmpleat}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void assignarEmpleat(@PathParam("nomAplicacio") String nomAplicacio,
                                @PathParam("nomModul") String nomModul,
                                @PathParam("idEmpleat") int idEmpleat) {
        BeanUtils.getBean(AssignarEmpleatModul.class).executar(nomAplicacio, nomModul, idEmpleat);
    }

    @DELETE
    @Path("empleat/delete/{nomAplicacio}/{nomModul}/{idEmpleat}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void eliminarEmpleatDelModul(@PathParam("nomAplicacio") String nomAplicacio,
                                        @PathParam("nomModul") String nomModul,
                                        @PathParam("idEmpleat") int idEmpleat) {
        BeanUtils.getBean(EliminarEmpleatDelModul.class).executar(nomAplicacio, nomModul, idEmpleat);
    }

    @GET
    @Path("empleats/tots/{nomAplicacio}/{nomModul}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Empleat> obtenirTotsElsEmpleatsByModul(@PathParam("nomAplicacio") String nomAplicacio,
                                                @PathParam("nomModul") String nomModul) {
        return BeanUtils.getBean(ObtenirTotsElsEmpleatsDelModul.class).executar(nomAplicacio, nomModul);
    }
}
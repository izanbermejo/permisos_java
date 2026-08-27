package ames.permisos.moduls;

import ames.permisos.moduls.internal.application.command.CrearModul;
import ames.permisos.moduls.internal.application.command.EliminarModul;
import ames.permisos.moduls.internal.application.command.ModificarModul;
import ames.permisos.moduls.internal.application.query.ObtenirModulByNom;
import ames.permisos.moduls.internal.application.query.ObtenirModulsByAplicacio;
import ames.permisos.moduls.internal.domain.Modul;
import ames.permisos.server.BeanUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Path("/moduls")
public class EndPointModuls {

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
    public void eliminarAplicacio(@PathParam("nomAplicacio") String nomAplicacio,
                                  @PathParam("nomModul") String nomModul) {
        BeanUtils.getBean(EliminarModul.class).executar(nomAplicacio, nomModul);
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
}
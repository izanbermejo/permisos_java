package ames.permisos.parametres;

import ames.permisos.parametres.internal.application.command.*;
import ames.permisos.parametres.internal.application.query.*;
import ames.permisos.parametres.internal.domain.EmpleatParametre;
import ames.permisos.parametres.internal.domain.FuncioParametre;
import ames.permisos.parametres.internal.domain.Parametre;
import ames.permisos.server.BeanUtils;
import ames.permisos.shared.ResultatEliminacio;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Path("/parametres")
public class EndPointParametres {

    @GET
    @Path("aplicacions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<Parametre> obtenirParametresByAplicacio(@QueryParam("nomAplicacions") List<String> request) {
        return BeanUtils.getBean(ObtenirParametresByAplicacio.class).executar(request);
    }

    @GET
    @Path("{nomAplicacio}/{nomParametre}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<Parametre> obtenirParametreByNom(@PathParam("nomAplicacio") String nomAplicacio,
                                             @PathParam("nomParametre") String nomParametre) {
        return BeanUtils.getBean(ObtenirParametreByNom.class).executar(nomAplicacio, nomParametre);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void crearParametre(Parametre parametre) {
        BeanUtils.getBean(CrearParametre.class).executar(parametre);
    }

    @PUT
    @Path("{nomAplicacio}/{nomParametre}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void modificarParametre(@PathParam("nomAplicacio") String nomAplicacio,
                               @PathParam("nomParametre") String nomParametre,
                               Parametre parametre) {
        BeanUtils.getBean(ModificarParametre.class).executar(nomAplicacio, nomParametre, parametre.descripcio());
    }

    @DELETE
    @Path("delete/{nomAplicacio}/{nomParametre}")
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultatEliminacio eliminarParametre(@PathParam("nomAplicacio") String nomAplicacio,
                                            @PathParam("nomParametre") String nomParametre,
                                            @QueryParam("confirmar") boolean confirmar) {
        return BeanUtils.getBean(EliminarParametre.class).executar(nomAplicacio, nomParametre, confirmar);
    }

    @GET
    @Path("funcions/{nomAplicacio}/{nomParametre}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<FuncioParametre> obtenirFuncionsByParametre(@PathParam("nomAplicacio") String nomAplicacio,
                                                    @PathParam("nomParametre") String nomParametre) {
        return BeanUtils.getBean(ObtenirFuncionsDelParametre.class).executar(nomAplicacio, nomParametre);
    }

    @POST
    @Path("funcio/{nomAplicacio}/{nomParametre}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void assignarFuncio(@PathParam("nomAplicacio") String nomAplicacio,
                               @PathParam("nomParametre") String nomParametre,
                               FuncioParametre funcioParametre) {
        BeanUtils.getBean(AssignarFuncioParametre.class).executar(nomAplicacio, nomParametre, funcioParametre);
    }

    @DELETE
    @Path("funcio/delete/{nomAplicacio}/{nomParametre}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void eliminarFuncioDelParametre(@PathParam("nomAplicacio") String nomAplicacio,
                                       @PathParam("nomParametre") String nomParametre,
                                       FuncioParametre funcioParametre) {
        BeanUtils.getBean(EliminarFuncioDelParametre.class).executar(nomAplicacio, nomParametre, funcioParametre);
    }

    @GET
    @Path("empleats/{nomAplicacio}/{nomParametre}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<EmpleatParametre> obtenirEmpleatsByParametre(@PathParam("nomAplicacio") String nomAplicacio,
                                                             @PathParam("nomParametre") String nomParametre) {
        return BeanUtils.getBean(ObtenirEmpleatsDelParametre.class).executar(nomAplicacio, nomParametre);
    }

    @POST
    @Path("empleat/{nomAplicacio}/{nomParametre}/{idEmpleat}")
    @Consumes(MediaType.TEXT_PLAIN)
    public void assignarEmpleat(@PathParam("nomAplicacio") String nomAplicacio,
                                @PathParam("nomParametre") String nomParametre,
                                @PathParam("idEmpleat") int idEmpleat,
                                @QueryParam("valor") String valor) {
        BeanUtils.getBean(AssignarEmpleatParametre.class).executar(nomAplicacio, nomParametre, valor, idEmpleat);
    }

    @DELETE
    @Path("empleat/delete/{nomAplicacio}/{nomParametre}/{idEmpleat}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void eliminarEmpleatDelParametre(@PathParam("nomAplicacio") String nomAplicacio,
                                            @PathParam("nomParametre") String nomParametre,
                                            @PathParam("idEmpleat") int idEmpleat) {
        BeanUtils.getBean(EliminarEmpleatDelParametre.class).executar(nomAplicacio, nomParametre, idEmpleat);
    }

    @GET
    @Path("empleats/tots/{nomAplicacio}/{nomParametre}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<EmpleatParametre> obtenirTotsElsEmpleatsByModul(@PathParam("nomAplicacio") String nomAplicacio,
                                                                @PathParam("nomParametre") String nomParametre) {
        return BeanUtils.getBean(ObtenirTotsElsEmpleatsDelParametre.class).executar(nomAplicacio, nomParametre);
    }
}
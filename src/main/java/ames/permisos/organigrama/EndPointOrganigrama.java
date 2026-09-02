package ames.permisos.organigrama;

import ames.permisos.organigrama.internal.application.query.ObtenirEmpleatsByCentDepFun;
import ames.permisos.organigrama.internal.application.query.ObtenirFuncionsByEmpleat;
import ames.permisos.organigrama.internal.domain.Empleat;
import ames.permisos.organigrama.internal.domain.Funcio;
import ames.permisos.server.BeanUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/organigrama")
public class EndPointOrganigrama {

    @GET
    @Path("empleats")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Empleat> obtenirEmpleatsByCentDepFun(
            @QueryParam("codiCentre") Integer codiCentre,
            @QueryParam("codiDepartament") Integer codiDepartament,
            @QueryParam("codiFuncio") Integer codiFuncio) {

        return BeanUtils.getBean(ObtenirEmpleatsByCentDepFun.class)
                .executar(codiCentre, codiDepartament, codiFuncio);
    }

    @GET
    @Path("funcions/empleat/{idEmpleat}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Funcio> obtenirFuncionsByEmpleat(
            @PathParam("idEmpleat") int idEmpleat) {

        return BeanUtils.getBean(ObtenirFuncionsByEmpleat.class)
                .executar(idEmpleat);
    }
}
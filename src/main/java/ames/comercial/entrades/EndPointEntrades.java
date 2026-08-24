package ames.comercial.entrades;

import ames.comercial.entrades.internal.application.*;
import ames.comercial.entrades.internal.application.query.*;
import ames.comercial.entrades.internal.application.query.BuscarEntradesComercial.BuscarEntradesComercialRequest;
import ames.comercial.entrades.internal.application.query.BuscarEntradesComercial.BuscarEntradesComercialResponse;
import ames.comercial.entrades.internal.application.query.BuscarEntradesMagatzem.BuscarEntradesMagatzemRequest;
import ames.comercial.entrades.internal.application.query.BuscarMissatges.BuscarMissatgesRequest;
import ames.comercial.entrades.internal.application.query.ObtenirEntradaDetallMagatzemByIdFabrica.DetallEntradaMagatzemResponse;
import ames.comercial.entrades.internal.application.query.ObtenirEntradaMagatzemByIdFabrica.ObtenirEntradaMagatzemByIdResponse;
import ames.comercial.entrades.internal.domain.EntradaComercial;
import ames.comercial.entrades.request.CanviArticleClientEntradaComercialRequest;
import ames.comercial.entrades.request.CanviOfEntradaComercialRequest;
import ames.comercial.entrades.request.GuardarAdresesEnviamentRequest;
import ames.comercial.server.BeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;

@Path("/entrada")
public class EndPointEntrades {

    private @Autowired ObjectMapper jsonMapper;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String buscarMissatges(@BeanParam BuscarMissatgesRequest request) {
        return BeanUtils.getBean(BuscarMissatges.class).executar(request).toString();
    }

    @GET
    @Path("/{id}/contingut")
    @Produces(MediaType.TEXT_PLAIN)
    public String obtenirContingutMissatge(@PathParam("id") String idEntradaFabrica) {
        return BeanUtils.getBean(ObtenirContingutMissatge.class).executar(idEntradaFabrica);
    }

    @GET
    @Path("/comercial")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<BuscarEntradesComercialResponse> buscarEntradesComercial (@BeanParam BuscarEntradesComercialRequest request,
                                                                          @DefaultValue("100") @QueryParam("limit") int limit){
        return BeanUtils.getBean(BuscarEntradesComercial.class).executar(request, limit);
    }

    @GET
    @Path("/comercial/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EntradaComercial> obtenirEntradaComercialByIdFabrica(@PathParam("id") String idEntradaFabrica) {
        return BeanUtils.getBean(ObtenirEntradaComercialByIdFabrica.class).executar(idEntradaFabrica);
    }

    @GET
    @Path("/magatzem")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<BuscarEntradesMagatzem.BuscarEntradesMagatzemResponse> buscarEntradesMagatzem (@BeanParam BuscarEntradesMagatzemRequest request){
        return BeanUtils.getBean(BuscarEntradesMagatzem.class).executar(request);
    }

    @GET
    @Path("/magatzem/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ObtenirEntradaMagatzemByIdResponse> obtenirEntradesMagatzem (@PathParam("id") String idEntrada) {
        return BeanUtils.getBean(ObtenirEntradaMagatzemByIdFabrica.class).executar(idEntrada);
    }

    @GET
    @Path("/magatzem/detall/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DetallEntradaMagatzemResponse> obtenirDetallEntradaMagatzemById (@PathParam("id") String idEntrada) {
        return BeanUtils.getBean(ObtenirEntradaDetallMagatzemByIdFabrica.class).executar(idEntrada);
    }

    @PUT
    @Path("/comercial/reprocessarEntrada/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void reprocessarEntradaComercial (@PathParam("id") String id) {
        BeanUtils.getBean(ReprocessaEntradaComercial.class).reprocessarEntradaComercial(id);
    }

    @PUT
    @Path("/comercial/canviArticleClient/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String canviArticleClientEntradaComercial (@PathParam("id") String id, CanviArticleClientEntradaComercialRequest req) {
        return BeanUtils.getBean(CanviArticleClientEntradaComercial.class).canviArticleClientEntradaComercial(id, req.client(), req.article());
    }

    @PUT
    @Path("/comercial/canviOf/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String canviOfEntradaComercial (@PathParam("id") String id, CanviOfEntradaComercialRequest req){
        return BeanUtils.getBean(CanviOfEntradaComercial.class).canviOfEntradaComercial(id, req.of());
    }

    @GET
    @Path("/comercial/exportarDades")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportarDadesComercial (@BeanParam BuscarEntradesComercialRequest request) throws IOException {
        byte[] bytes = BeanUtils.getBean(ExportarDadesComercial.class).exportar(request);
        return Response.ok(bytes)
                .header("Content-Disposition", "attachment;filename=Variacio.xlsx")
                .build();
    }

    @GET
    @Path("/emailsEnviament")
    @Produces(MediaType.APPLICATION_JSON)
    public String obtenirEmailsEnviament() {
        return BeanUtils.getBean(ObtenirEmailsEnviament.class).executar().toString();
    }

    @PUT
    @Path("/emailsEnviament")
    @Produces(MediaType.APPLICATION_JSON)
    public void guardarEmailsEnviament (GuardarAdresesEnviamentRequest req) {
        BeanUtils.getBean(GuardarEmailsEnviament.class).executar(req.emailsComercial(), req.emailsMagatzem());
    }
}

package ames.comercial.clients;

import ames.comercial.clients.internal.application.command.AfegirAdjuntClient;
import ames.comercial.clients.internal.application.command.EliminarAdjuntClient;
import ames.comercial.clients.internal.application.command.RenombrarAdjuntClient;
import ames.comercial.clients.internal.application.query.ObtenirAdjuntClient;
import ames.comercial.clients.internal.application.query.ObtenirAdjuntClientFile;
import ames.comercial.clients.internal.application.query.ObtenirAdjuntsClient;
import ames.comercial.clients.internal.application.query.ObtenirAdjuntsClient.ObtenirAdjuntsClientResponse;
import ames.comercial.clients.internal.application.query.ObtenirAdjuntsClient.ObtenirAdjuntsClientRequest;
import ames.comercial.comandes.internal.application.command.AfegirAdjuntComanda;
import ames.comercial.comandes.internal.application.command.EliminarAdjuntComanda;
import ames.comercial.comandes.internal.application.command.RenombrarAdjuntComanda;
import ames.comercial.comandes.internal.application.query.ObtenirAdjuntComanda;
import ames.comercial.comandes.internal.application.query.ObtenirAdjuntComandaFile;
import ames.comercial.comandes.request.StringRequest;
import ames.comercial.server.BeanUtils;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Path("/client")
public class EndPointClients {

    @GET
    @Path("/{codiClient}/adjunts")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ObtenirAdjuntsClientResponse> obtenirAdjuntsClient (@BeanParam ObtenirAdjuntsClientRequest request,
                                                                    @PathParam("codiClient") String codiClient){
        return BeanUtils.getBean(ObtenirAdjuntsClient.class).executar(codiClient, request);
    }

    @POST
    @Path("/{codiClient}/adjunts/{categoria}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void pujarAdjunt(@PathParam("codiClient") String codiClient,
                            @PathParam("categoria") long categoria,
                            @FormDataParam("file") InputStream inputStream,
                            @FormDataParam("file") FormDataContentDisposition fileDetail) {
        String nomFitxer = new String(fileDetail.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        BeanUtils.getBean(AfegirAdjuntClient.class).executar(codiClient, categoria, inputStream, nomFitxer);
    }

    @GET
    @Path("/{codiClient}/adjunts/{codiAdjunt}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response descarregarAdjunt(@PathParam("codiClient") String codiClient, @PathParam("codiAdjunt") String codiAdjunt) {
        var nomFile = BeanUtils.getBean(ObtenirAdjuntClient.class).executar(codiClient, codiAdjunt).nom();
        var file = BeanUtils.getBean(ObtenirAdjuntClientFile.class).executar(codiClient, codiAdjunt);
        return Response.status(200)
                .entity(file)
                .header("Content-Disposition", "attachment; filename=\"" + nomFile + "\"")
                .build();
    }

    @PUT
    @Path("/{codiClient}/adjunts/{codiAdjunt}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void renombrarAdjunt(@PathParam("codiClient") String codiClient, @PathParam("codiAdjunt") String codiAdjunt, StringRequest req) {
        BeanUtils.getBean(RenombrarAdjuntClient.class).executar(codiClient, codiAdjunt, req.valor());
    }

    @DELETE
    @Path("/{codiClient}/adjunts/{codiAdjunt}")
    public void eliminarAdjunt(@PathParam("codiClient") String codiClient, @PathParam("codiAdjunt") String codiAdjunt) {
        BeanUtils.getBean(EliminarAdjuntClient.class).executar(codiClient, codiAdjunt);
    }
}

package ames.comercial.magatzem;

import ames.comercial.magatzem.reports.Localitzacio;
import ames.comercial.magatzem.reports.Trasabilitat;
import ames.comercial.server.BeanUtils;
import ames.comercial.shared.KeyArticleClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("ext/magatzem")
public class EndPointMagatzemExt {

    @GET
    @Path("/localitzacio")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response reportLocalitzacio(
            @QueryParam("artInt") String articleParam,
            @QueryParam("codClient") String codClient) throws Exception {

        KeyArticleClient keyArticleClient = KeyArticleClient.of(articleParam, codClient);

        byte[] bytes = BeanUtils.getBean(Localitzacio.class)
                .generateReport(keyArticleClient)
                .toByteArray();

        String fileName = String.format("Localitzacio%s%s.xlsx", keyArticleClient.artint(), keyArticleClient.clicod());

        return Response.ok(bytes)
                .type("application/vnd.ms-excel")
                .header("Content-Disposition", "attachment;filename=\"" + fileName + "\"")
                .build();
    }


    @GET
    @Path("/trasabilitat")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response reportTrasabilitat(
            @QueryParam("artInt") String articleParam,
            @QueryParam("codClient") String codClient) throws Exception {

        KeyArticleClient keyArticleClient = KeyArticleClient.of(articleParam, codClient);

        byte[] bytes = BeanUtils.getBean(Trasabilitat.class)
                .generateReport(keyArticleClient)
                .toByteArray();

        String fileName = String.format("Trasabilitat%s%s.xls",
                keyArticleClient.artint(),
                keyArticleClient.clicod());

        return Response.ok(bytes)
                .type("application/vnd.ms-excel")
                .header("Content-Disposition", "attachment;filename=\"" + fileName + "\"")
                .build();
    }


}

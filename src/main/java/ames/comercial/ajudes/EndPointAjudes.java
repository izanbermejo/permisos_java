package ames.comercial.ajudes;

import ames.comercial.advantage.internal.response.ObtenirArticlesClientsResponse;
import ames.comercial.advantage.internal.response.QueryArticleNormalitzatResponse;
import ames.comercial.ajudes.internal.ObtenirArticles;
import ames.comercial.ajudes.internal.ObtenirArticles.QueryArticleResponse;
import ames.comercial.ajudes.internal.ObtenirArticlesClients;
import ames.comercial.ajudes.internal.ObtenirArticlesClientsRequestImpl;
import ames.comercial.ajudes.internal.ObtenirArticlesNormalitzats;
import ames.comercial.server.BeanUtils;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.TipusArticleClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/ajuda")
public class EndPointAjudes {

    @GET
    @Path("articlesnormalitzats")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<QueryArticleNormalitzatResponse> obtenirArticlesNormalitzats(
            @QueryParam("empresa") @DefaultValue("40") String empresa,
            @QueryParam("tipus") String tipus,
            @QueryParam("filtre") String filtre) {
        return BeanUtils.getBean(ObtenirArticlesNormalitzats.class).query(filtre, Empresa.getByClau(empresa), TipusArticleClient.valueOf(tipus));
    }

    @GET
    @Path("articles")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<QueryArticleResponse> obtenirArticles(
            @QueryParam("client") String client,
            @QueryParam("tipus") String tipus,
            @QueryParam("filtre") String filtre) {
        return BeanUtils.getBean(ObtenirArticles.class).query(filtre, client, TipusArticleClient.valueOf(tipus));
    }

    @GET
    @Path("articleclient")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ObtenirArticlesClientsResponse> obtenirArticlesClient(@QueryParam("filtre") String filtre,
                                                                      @DefaultValue("false") @QueryParam("inactius") boolean incloureInactius,
                                                                      @DefaultValue("") @QueryParam("colOrder") String colOrder,
                                                                      @DefaultValue("false") @QueryParam("orderAsc") boolean asc) {
        var request = ObtenirArticlesClientsRequestImpl.builder()
                .filtre(filtre)
                .incloureInactius(incloureInactius)
                .colOrder(colOrder)
                .asc(asc)
                .build();
        return BeanUtils.getBean(ObtenirArticlesClients.class).query(request);
    }

    @GET
    @Path("articleclientperarticle/{artint}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ObtenirArticlesClientsResponse> obtenirArticlesClientByArticle(@PathParam("artint") String artint,
                                                                               @QueryParam("filtre") String filtre,
                                                                               @DefaultValue("false") @QueryParam("inactius") boolean incloureInactius,
                                                                               @DefaultValue("") @QueryParam("colOrder") String colOrder,
                                                                               @DefaultValue("false") @QueryParam("orderAsc") boolean asc) {
        var request = ObtenirArticlesClientsRequestImpl.builder()
                .artint(artint)
                .filtre(filtre)
                .incloureInactius(incloureInactius)
                .colOrder(colOrder)
                .asc(asc)
                .build();
        return BeanUtils.getBean(ObtenirArticlesClients.class).query(request);
    }

    @GET
    @Path("client/{codiClient}/articleclient")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<ObtenirArticlesClientsResponse> obtenirArticlesClientsByClient(@PathParam("codiClient") String codiClient,
                                                                           @DefaultValue("") @QueryParam("filtre") String filtre,
                                                                           @DefaultValue("false") @QueryParam("inactius") boolean incloureInactius,
                                                                           @DefaultValue("") @QueryParam("colOrder") String colOrder,
                                                                           @DefaultValue("false") @QueryParam("orderAsc") boolean asc) {
        var request = ObtenirArticlesClientsRequestImpl.builder()
                .clicod(codiClient)
                .filtre(filtre)
                .incloureInactius(incloureInactius)
                .colOrder(colOrder)
                .asc(asc)
                .build();
        return BeanUtils.getBean(ObtenirArticlesClients.class).query(request);
    }

}

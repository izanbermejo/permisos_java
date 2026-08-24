package ames.comercial.cache;

import ames.comercial.cache.articlesclient.ReompleCacheArticlesClient;
import ames.comercial.cache.clients.ReompleCacheClients;
import ames.comercial.cache.empleats.ReompleCacheEmpleats;
import ames.comercial.server.BeanUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("ext/cache")
public class EndPointCache {

    @GET
    @Path("clients")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateCacheClients() {
        BeanUtils.getBean(ReompleCacheClients.class).executar();
    }

    @GET
    @Path("articlesclient")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateCacheArticlesclient() {
        BeanUtils.getBean(ReompleCacheArticlesClient.class).executar();
    }

    @GET
    @Path("empleats")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateCacheEmpleats() {
        BeanUtils.getBean(ReompleCacheEmpleats.class).executar();
    }

}

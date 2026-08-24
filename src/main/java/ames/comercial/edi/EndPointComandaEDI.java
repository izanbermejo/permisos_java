package ames.comercial.edi;

import ames.comercial.advantage.IObtenirClientAds;
import ames.comercial.advantage.internal.ObtenirAlbaransFacturesByArticleAndClientAds;
import ames.comercial.advantage.internal.ObtenirArticleClientEDIAds;
import ames.comercial.advantage.internal.ObtenirClientEDIAds;
import ames.comercial.advantage.internal.response.QueryAlbaransFacturesByArticleAndClientResponse;
import ames.comercial.edi.beans.ComandaMissatgeEDI;
import ames.comercial.edi.internal.application.command.EsborraArticleComanda;
import ames.comercial.edi.internal.application.command.EsborraComanda;
import ames.comercial.edi.internal.application.command.ModificaUltimAlbara;
import ames.comercial.edi.internal.application.command.ProcesaLiniaArticle;
import ames.comercial.edi.internal.application.query.*;
import ames.comercial.edi.internal.domain.DadesComandaEDI;
import ames.comercial.edi.internal.domain.DadesComandaEDINoJSON;
import ames.comercial.edi.internal.domain.DadesMissatgeEDIPerArticle;
import ames.comercial.edi.internal.infraestructure.comandaEDI.ComandaEDIRepository;
import ames.comercial.edi.request.LiniaArticleEditRequest;
import ames.comercial.edi.response.ComandaEDIResponse;
import ames.comercial.server.BeanUtils;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleAmes;
import ames.comercial.shared.Usuari;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Path("/comandes")
public class EndPointComandaEDI {

    @Autowired
    ComandaEDIRepository comandaEDIRepo;

    @Autowired
    ComandaEDIConfig config;

    @Autowired
    IObtenirClientAds obtenirClientAds;

    @Autowired
    ObtenirClientEDIAds obtenirClientEDIAds;

    @Autowired
    ObtenirArticleClientEDIAds obtenirArticleClientEDIAds;

    @Autowired
    ObtenirAlbaransFacturesByArticleAndClientAds obtenirAlbaransByArticleAndClientAds;

    static final Logger log = LogManager.getLogger(EndPointComandaEDI.class);

    @Autowired
    GenerarPdfProgramaEntregaEdiFromTxt generarPdfProgramaEntregaEdi;

//    @GET
//    @Path("EDI/{codi}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces(MediaType.APPLICATION_JSON)
//    public Optional<DadesComandaEDI> obtenirComandaEDI(@PathParam("codi") String codi) {
//        return new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).getOrder(Long.valueOf(codi));
//    }
//
//    @GET
//    @Path("EDI/{codi}/verbose")
//    @Consumes(MediaType.APPLICATION_JSON)
//    @Produces({MediaType.APPLICATION_JSON})
//    public Pedido obtenirComandaEDIpedido(@PathParam("codi") String codi) {
//        return new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).getOrderJSON(Long.valueOf(codi));
//    }

    @GET
    @Path("EDI")
    @Produces({MediaType.APPLICATION_JSON})
    public List<DadesComandaEDINoJSON> llistatDeComandesEDI() {
        List<DadesComandaEDINoJSON> list = BeanUtils.getBean(ObtenirComandesEDI.class).executar();
        return list;
    }

    @GET
    @Path("EDI/usuari/{usuari}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<DadesComandaEDINoJSON> llistatDeComandesEDI(@PathParam("usuari") String usuari) {
//        List<DadesComandaEDINoJSON> list = new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).listOrders();
        List<DadesComandaEDINoJSON> list = BeanUtils.getBean(ObtenirComandesEDI.class).executar(usuari);
        return list;
    }

    @GET
    @Path("EDI/linies/{codi_comanda}")
    @Produces({MediaType.APPLICATION_JSON})
    public ComandaEDIResponse llistatDeLiniesEDI(@PathParam("codi_comanda") Long codi_comanda, @QueryParam("first_article") Boolean first_article) {
//        ComandaEDIResponse listPendentsDeServir = new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).listLinesDeComandesPendentsDeServir(codi_comanda,null,first_article);
        ComandaEDIResponse listPendentsDeServir = BeanUtils.getBean(ObtenirLiniesComandesEDI.class).executar(codi_comanda,null,first_article);
        return listPendentsDeServir;
    }

    @GET
    @Path("EDI/linies/{codi_comanda}/{codi_article}")
    @Produces({MediaType.APPLICATION_JSON})
    public ComandaEDIResponse llistatDeLiniesEDIPerActicle(@PathParam("codi_comanda") Long codi_comanda,@PathParam("codi_article") String codi_article) {
//        ComandaEDIResponse listPendentsDeServir = new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).listLinesDeComandesPendentsDeServir(codi_comanda,codi_article,null);
        ComandaEDIResponse listPendentsDeServir = BeanUtils.getBean(ObtenirLiniesComandesEDI.class).executar(codi_comanda,codi_article,null);
        return listPendentsDeServir;
    }

    @GET
    @Path("EDI/linies/articles/{codi_comanda}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<KeyArticleAmes> llistatArticlesEDI(@PathParam("codi_comanda") Long codi_comanda) {
//        List<KeyArticleAmes> list = new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).listProducts(codi_comanda);
        List<KeyArticleAmes> list = BeanUtils.getBean(ObtenirArticlesDeComandaEDI.class).executar(codi_comanda);
        return list;
    }

    @GET
    @Path("EDI/usuaris")
    @Produces({MediaType.APPLICATION_JSON})
    public List<Usuari> llistatUsuarisEDI() {
//        List<KeyArticleAmes> list = new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).listProducts(codi_comanda);
        List<Usuari> list = BeanUtils.getBean(ObtenirUsuarisDeComandaEDI.class).executar();
        return list;
    }


//    @PUT
//    @Path("EDI/{codiComanda}/{codiArticle}/process")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response processOrder(@PathParam("codiComanda") Long codiComanda, @PathParam("codiArticle") Long codiArticle) {
//        String user = "DEV_PENDING_USER_FROM_CREDENTIALS"; //TODO pending to get user from "web app credentials" keycloack
//
//        //TODO
//        // 1) Carrego totes les linies d'una comanda i article
//        // 2) Valido que hi hagin totes les dades necessaries informades i correctes tal com necessita la API de comandes
//        // 3) Identifico segons el client, de quina "tipologia" es.
//        // 4) Aplico els "ajustos"
//
//        return Response.ok().build();
//    }

    @GET
    @Path("EDI/pdf/{codiComanda}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPDF(@PathParam("codiComanda") Long codiComanda) {
        byte[] bytes = null;
        try {
//            Optional<DadesComandaEDI> comandaEDI = new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).getOrder(codi_comanda);
            Optional<DadesComandaEDI> comandaEDI = BeanUtils.getBean(ObtenirComandaEDI.class).executar(codiComanda);
            if (!comandaEDI.isEmpty()) {
                if (!comandaEDI.get().pathPDF().isEmpty()) {
                    bytes = Files.readAllBytes(Paths.get(comandaEDI.get().pathPDF().get()));
                    return Response.ok(bytes)
                            .header("Content-Disposition", "attachment;filename=" + Paths.get(comandaEDI.get().pathPDF().get()).getFileName())
                            .build();
                } else
                    return Response.noContent().build();
            } else
                return Response.noContent().build();
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @GET
    @Path("EDI/txt/{codiComanda}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadTXT(@PathParam("codiComanda") Long codiComanda) {
        byte[] bytes = null;
        try {
//            Optional<DadesComandaEDI> comandaEDI = new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).getOrder(codi_comanda);
            Optional<DadesComandaEDI> comandaEDI = BeanUtils.getBean(ObtenirComandaEDI.class).executar(codiComanda);
            if (!comandaEDI.isEmpty()) {
                    bytes = Files.readAllBytes(Paths.get(comandaEDI.get().pathEDI()));
                    return Response.ok(bytes)
                            .header("Content-Disposition", "attachment;filename=" + Paths.get(comandaEDI.get().pathEDI()).getFileName())
                            .build();
            } else
                return Response.noContent().build();
        } catch (IOException e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return Response.serverError().build();
        }
    }

    @GET
    @Path("EDI/json/{codiComanda}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComandaMissatgeEDI downloadJSON(@PathParam("codiComanda") Long codiComanda) {
            return BeanUtils.getBean(ObtenirJSONComandaEDI.class).executar(codiComanda);
    }

    @DELETE
    @Path("EDI/{codi_comanda}")
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteOrder(@PathParam("codi_comanda") Long codiComanda) {
//        new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).delete(Long.valueOf(codi_comanda));
        BeanUtils.getBean(EsborraComanda.class).executar(codiComanda);
    }

//    @DELETE
//    @Path("EDI/linia/{codi}")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response deleteOrderLine(@PathParam("codi") Long codi) {
//        new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).deleteLine(Long.valueOf(codi));
//        return Response.ok().entity("Linia de comanda eliminada").build();
//    }

    @POST
    @Path("EDI/delete/linia/{codi}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComandaEDIResponse deleteOrderLinePong(@PathParam("codi") Long codi,ComandaEDIResponse comanda) {
//        return new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).deleteOrderLinePong(codi,comanda);
        return BeanUtils.getBean(RecalculEliminaLiniaComandaEDI.class).executar(codi,comanda);
    }

    @DELETE
    @Path("EDI/delete/article/{codiComanda}/{codiArticle}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void deleteOrderLineArticle(@PathParam("codiComanda") Long codiComanda,@PathParam("codiArticle")String codiArticle) {
//        return new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).deleteOrderLinePong(codi,comanda);
        BeanUtils.getBean(EsborraArticleComanda.class).executar(codiComanda,codiArticle);
    }

    @POST
    @Path("EDI/edit/linia/{codi}/{codiLiniaProcessat}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComandaEDIResponse editOrderLinePong(@PathParam("codi") Long codi,@PathParam("codiLiniaProcessat") Long codiLiniaProcessat, LiniaArticleEditRequest comanda) {
//        return new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).editOrderLinePong(codi,request);
        return BeanUtils.getBean(RecalculModificaLiniaComandaEDI.class).executar(codi,codiLiniaProcessat,comanda);
    }

    @POST
    @Path("EDI/edit/article/{articleClient}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComandaEDIResponse editOrderArticleClientPong(@PathParam("articleClient") String articleClient, ComandaEDIResponse comanda) {
//        return new ComandaEDIService(comandaEDIRepo, config, obtenirClientAds, obtenirArticleClientEDIAds, obtenirAlbaransByArticleAndClientAds,obtenirStocksAds,obtenirClientEDIAds,obtenirComandesPerEDIAds).editOrderLinePong(codi,request);
        return BeanUtils.getBean(RecalculModificaArticleClientLiniaComandaEDI.class).executar(articleClient,comanda);
    }

    @GET
    @Path("EDI/albarans/{article}/{client}")
    @Consumes (MediaType.APPLICATION_JSON)
    @Produces (MediaType.APPLICATION_JSON)
    public List<QueryAlbaransFacturesByArticleAndClientResponse> llistatUltimsAlbarans(@PathParam("article") String codiArticle, @PathParam("client") String codiClient, @QueryParam("ultimalbararebut") String ultimAlbaraRebut,
                                                                                       @QueryParam("tipusClient") String tipusClient) {
        return BeanUtils.getBean(ObtenirAlbaransEDI.class).executar(codiArticle,codiClient,ultimAlbaraRebut,tipusClient);
    }

    @POST
    @Path("EDI/process/{codiComanda}/{codiArticle}")
    @Consumes (MediaType.APPLICATION_JSON)
    public void processaArticleLiniesComanda(@PathParam("codiComanda") Long codiComanda, @PathParam("codiArticle") String codiArticle, ComandaEDIResponse comanda) {
        BeanUtils.getBean(ProcesaLiniaArticle.class).executar(codiComanda,codiArticle,comanda);
    }


    @POST
    @Path("EDI/albarans/{codiComanda}/{codiArticle}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public void updateUltimAlbaraPong(@PathParam("codiComanda") Long codiComanda, @PathParam("codiArticle") String codiArticle,String ultimAlbara) {
        BeanUtils.getBean(ModificaUltimAlbara.class).executar(codiComanda,codiArticle,ultimAlbara);
    }

    @GET
    @Path("EDI/pdf/generar")
    @Produces("application/pdf")
    public Response generarPdfProgramaEntrega(@QueryParam("pathEdi") String pathEdi) {
        byte[] pdf = generarPdfProgramaEntregaEdi.run(pathEdi, RequestThread.idioma());
        return Response.ok(pdf)
                .header("Content-Disposition", "inline; filename=\"programa_entrega_" + pathEdi + ".pdf\"")
                .build();
    }

    @GET
    @Path("EDI/missatgesPerArticleClient/{cliCod}/{artInt}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<DadesMissatgeEDIPerArticle> missatgesPerArticleClient(
            @PathParam("cliCod") String cliCod,
            @PathParam("artInt") String artInt,
            @QueryParam("dataInici") String dataInici,
            @QueryParam("dataFi") String dataFi) {
        return BeanUtils.getBean(ObtenirMissatgesEDIPerArticleClient.class)
                .executar(cliCod, artInt, LocalDate.parse(dataInici), LocalDate.parse(dataFi));
    }

}

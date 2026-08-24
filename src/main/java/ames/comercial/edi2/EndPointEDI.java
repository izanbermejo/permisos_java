package ames.comercial.edi2;

import ames.comercial.comandes.request.StringRequest;
import ames.comercial.edi2.internal.application.*;
import ames.comercial.edi2.internal.application.query.*;
import ames.comercial.edi2.internal.application.query.BuscarArxius.BuscarArxiusRequest;
import ames.comercial.edi2.internal.application.query.ObtenirComandesEDI2.ObtenirComandesEDI2Request;
import ames.comercial.edi2.internal.application.service.GuardarPDF;
import ames.comercial.edi2.internal.domain.ConfiguracioAviExp;
import ames.comercial.edi2.internal.domain.ConfiguracioEdi;
import ames.comercial.edi2.internal.domain.ConfiguracioEntradaComanda;
import ames.comercial.edi2.internal.domain.comanda.ComandaEdi;
import ames.comercial.edi2.internal.domain.comanda.KeyComandaEdi;
import ames.comercial.edi2.internal.domain.embalatgeexpedicio.EmbalatgeExpedicio;
import ames.comercial.edi2.internal.infraestructure.configuracio.entradesedi.ConfiguracioEntradesEntradesEdiSql;
import ames.comercial.edi2.request.AssignacioManualRequest;
import ames.comercial.edi2.request.ProcessarComandaRequest;
import ames.comercial.edi2.response.CapsaleraProcessarEDIResponse;
import ames.comercial.edi2.response.MergeComandaResponse;
import ames.comercial.server.BeanUtils;
import ames.comercial.server.RequestThread;
import ames.comercial.shared.KeyArticleClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Path("/edi")
public class EndPointEDI {

    private @Autowired ObjectMapper jsonMapper;

    @GET
    @Path("/buscarArxius")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String buscarArxius(@BeanParam BuscarArxiusRequest request) {
        return BeanUtils.getBean(BuscarArxius.class).executar(request).toString();
    }

    @GET
    @Path("/comanda")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<ComandaEdi> obtenirComandaEdi(@QueryParam("idMissatge") long idMissatge, @QueryParam("idComanda") long idComanda) {
        return BeanUtils.getBean(ObtenirComandaEdi2.class).executar(KeyComandaEdi.of(idMissatge, idComanda));
    }

    @GET
    @Path("/comandes")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String obtenirComandesEdi(@BeanParam ObtenirComandesEDI2Request request) {
        return BeanUtils.getBean(ObtenirComandesEDI2.class).executar(request).toString();
    }

    @GET
    @Path("/comandes/client/{codiClient}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String obtenirComandesEdiByClient(@PathParam("codiClient") String codiClient) {
        return BeanUtils.getBean(ObtenirComandesEDI2ByClient.class).executar(codiClient).toString();
    }

    @GET
    @Path("/comandes/articleclient/{articleClient}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String obtenirComandesEdiByArticleClient(@PathParam("articleClient") String articleClient) {
        return BeanUtils.getBean(ObtenirComandesEDI2ByArticleClient.class).executar(articleClient).toString();
    }

    @GET
    @Path("/comandes/missatge/{codiMissatge}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String obtenirComandesEdiByMissatge(@PathParam("codiMissatge") String codiMissatge) {
        return BeanUtils.getBean(ObtenirComandesEDI2ByMissatge.class).executar(codiMissatge).toString();
    }

    @GET
    @Path("/comandes/txt/{idMissatge}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String obtenirContingutTXTByMissatge(@PathParam("idMissatge") String idMissatge) {
        return BeanUtils.getBean(ObtenirContingutTXTByMissatge.class).executar(idMissatge).toString();
    }

    @GET
    @Path("/comandes/pdf/{idMissatge}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response descarregarPDF(@PathParam("idMissatge") String idMissatge, @QueryParam("pathPDF") String pathPDF) {
        var nomFile = BeanUtils.getBean(ObtenirPdfEDI.class).obtenirNomPDF(pathPDF);
        var file = BeanUtils.getBean(ObtenirPdfEDI.class).executar(pathPDF);
        return Response.status(200)
                .entity(file)
                .header("Content-Disposition", "attachment; filename=" + nomFile)
                .build();
    }

    @POST
    @Path("/comandes/pdf/{idMissatge}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void pujarPDF(@FormDataParam("file") InputStream inputStream,
                         @FormDataParam("file") FormDataContentDisposition fileDetail,
                         @PathParam("idMissatge") long idMissatge) throws IOException {
        String nomFitxer = new String (fileDetail.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        BeanUtils.getBean(GuardarPDF.class).guardar(idMissatge, nomFitxer, inputStream);
    }

    @GET
    @Path("/comandes/pdf/generar/{idMissatge}")
    @Produces("application/pdf")
    public Response generarPdfProgramaEntrega(@PathParam("idMissatge") long idMissatge) {
        byte[] pdf = BeanUtils.getBean(GenerarPdfProgramaEntregaEdi.class).run(idMissatge, RequestThread.idioma());
        return Response.ok(pdf)
                .header("Content-Disposition", "inline; filename=programa_entrega_" + idMissatge + ".pdf")
                .build();
    }

    @GET
    @Path("/entrada/configuracions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String obtenirConfiguracionsEDI(@QueryParam("filtre") String filtre,
                                           @DefaultValue("100") @QueryParam("limit") int limit) {
        return BeanUtils.getBean(ObtenirConfiguracionsEDI2.class).executar(filtre, limit).toString();
    }

    @GET
    @Path("/entrada/configuracio")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConfiguracioEntradaComanda carregarConfiguracioEDI(@QueryParam("codiClient") String codiClient,
                                                              @QueryParam("tipusMissatge") String tipusMissatge) {
        return BeanUtils.getBean(ConfiguracioEntradesEntradesEdiSql.class).obtenirConfiguracioEdi(codiClient, tipusMissatge);
    }

    @PUT
    @Path("/entrada/configuracio")
    @Consumes(MediaType.APPLICATION_JSON)
    public void guardarConfiguracioEDI(ConfiguracioEntradaComanda req) {
        BeanUtils.getBean(GuardarConfiguracioEDI2.class).guardar(req);
    }

    @GET
    @Path("/comandes/processar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public MergeComandaResponse ObtenirComandaPerProcessar(@QueryParam("idMissatge") long idMissatge,
                                                           @QueryParam("idComanda") long idComanda,
                                                           @QueryParam("ultimAlbaraReferencia") @DefaultValue("") String albaraReferencia) throws Exception {
        var key = KeyComandaEdi.of(idMissatge, idComanda);
        return BeanUtils.getBean(ObtenirComandaPerProcessar.class).executar(key, albaraReferencia);
    }

    @GET
    @Path("/configuracions/exportar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportarConfiguracionsEdi () throws IOException {
        byte[] bytes = BeanUtils.getBean(ExportarConfiguracionsEdi2.class).exportar();
        return Response.ok(bytes)
                .header("Content-Disposition", "attachment;filename=conf_edi.xlsx")
                .build();
    }

    @GET
    @Path("/configuracions/exportar/aviexp")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportarConfiguracionsAviExp() throws IOException {
        byte[] bytes = BeanUtils.getBean(ExportarConfiguracionsAviExp.class).exportar();
        return Response.ok(bytes)
                .header("Content-Disposition", "attachment;filename=conf_aviexp.xlsx")
                .build();
    }

    @POST
    @Path("/comandes/reprocessar/{idMissatge}/{idComanda}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Optional<String> reprocessarComanda(@PathParam("idMissatge") long idMissatge, @PathParam("idComanda") long idComanda) {
        var key = KeyComandaEdi.of(idMissatge, idComanda);
        return BeanUtils.getBean(AssignarArtIntClicodComandaEdi.class).processarComanda(key);
    }

    @POST
    @Path("/comandes/assignar")
    @Consumes(MediaType.APPLICATION_JSON)
    public Optional<String> assignacioManual(AssignacioManualRequest req) {
        var keyEdi = KeyComandaEdi.of(req.idMissatge(), req.idComanda());
        var keyArtCli = KeyArticleClient.of(req.artInt(), req.cliCod());
        return BeanUtils.getBean(AssignarArtIntComandaEdi.class).executar(keyArtCli, keyEdi);
    }

    @GET
    @Path("/comandes/processar/capsalera")
    @Produces(MediaType.APPLICATION_JSON)
    public CapsaleraProcessarEDIResponse ObtenirCapsaleraProcessarEdi(@QueryParam("articleClient") String articleClient){
        return BeanUtils.getBean(ObtenirCapsaleraProcessarEdi.class).executar(articleClient);
    }

    @POST
    @Path("/comandes/processar")
    @Consumes(MediaType.APPLICATION_JSON)
    public void ProcessarComandaEdi(ProcessarComandaRequest req){
        var keyEdi = KeyComandaEdi.of(req.idMissatge(), req.idComanda());
        var keyArtCli = KeyArticleClient.of(req.artInt(), req.cliCod());
        BeanUtils.getBean(ProcessarComandaEdi.class).executar(keyEdi, keyArtCli, req.linies());
    }

    @POST
    @Path("/comandes/eliminar/{idMissatge}/{idComanda}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void eliminarComandaEDI(@PathParam("idMissatge") long idMissatge, @PathParam("idComanda") long idComanda) {
        var key = KeyComandaEdi.of(idMissatge, idComanda);
        BeanUtils.getBean(EliminarComandaEDI.class).executar(key);
    }

    @GET
    @Path("/embalatge/{artInt}/{cliCod}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<EmbalatgeExpedicio> obtenirComandesEdiByClient(@PathParam("artInt") String artInt, @PathParam("cliCod") String cliCod) {
        var keyArtCli = KeyArticleClient.of(artInt, cliCod);
        return BeanUtils.getBean(ObtenirEmbalatgesExpedicio.class).executar(keyArtCli);
    }

    @PUT
    @Path("/embalatge/{artInt}/{cliCod}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void guardarEmbalatgeExpedicio(@PathParam("artInt") String artInt, @PathParam("cliCod") String cliCod, EmbalatgeExpedicio req) {
        var keyArtCli = KeyArticleClient.of(artInt, cliCod);
        BeanUtils.getBean(GuardarEmbalatgesExpedicio.class).executar(keyArtCli, req);
    }

    @GET
    @Path("/sortida/configuracio")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String obtenirConfiguracioAviExp(@QueryParam("filtre") String filtre,
                                            @DefaultValue("100") @QueryParam("limit") int limit){
        return BeanUtils.getBean(ObtenirConfiguracionsAviExp.class).executar(filtre, limit).toString();
    }

    @GET
    @Path("/carrega/configuracio")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<ConfiguracioAviExp> carregarConfiguracioAviExp(@QueryParam("codiClient") String codiClient,
                                                         @QueryParam("codiProveidor") String codiProveidor) {
        return BeanUtils.getBean(CarregaConfiguracioAviExp.class).get(codiClient, codiProveidor);
    }

    @PUT
    @Path("/guarda/configuracio")
    @Consumes(MediaType.APPLICATION_JSON)
    public void guardarConfiguracioAviExp(ConfiguracioAviExp req) {
        BeanUtils.getBean(GuardarConfiguracioAviExp.class).guardar(req);
    }

    @GET
    @Path("/configuracio/{codiClient}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ConfiguracioEdi carregarConfiguracioEdi(@PathParam("codiClient") String codiClient) {
        return BeanUtils.getBean(ObtenirConfiguracioEdi.class).get(codiClient);
    }

    @PUT
    @Path("/{idMissatge}/{idComanda}/comentarisinterns")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void actualitzarComentarisInternsLiniaComanda(@PathParam("idMissatge") long idMissatge,
                                                         @PathParam("idComanda") long idComanda,
                                                         @QueryParam("idLinia") long idLinia, StringRequest req) {
        BeanUtils.getBean(ActualitzarComentarisInternsLiniaEDI.class).executar(KeyComandaEdi.of(idMissatge,idComanda), idLinia, req.valor());
    }

    @PUT
    @Path("/{idMissatge}/{idComanda}/comentarisclient")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void actualitzarComentarisClientLiniaComanda(@PathParam("idMissatge") long idMissatge,
                                                        @PathParam("idComanda") long idComanda,
                                                        @QueryParam("idLinia") long idLinia, StringRequest req) {
        BeanUtils.getBean(ActualitzarComentarisClientLiniaEDI.class).executar(KeyComandaEdi.of(idMissatge,idComanda), idLinia, req.valor());
    }

}
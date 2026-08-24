package ames.comercial.advantage;

import ames.comercial.advantage.internal.*;
import ames.comercial.advantage.internal.ObtenirAltreInfoArticleClient.AltreInfoArticleClient;
import ames.comercial.advantage.internal.ObtenirAltreInfoClient.AltreInfoClient;
import ames.comercial.advantage.internal.ObtenirArticleClientNormalitzatsAds.ObtenirArticleClientNormalitzatsAdsResponse;
import ames.comercial.advantage.internal.ObtenirClientAds.ClientAds;
import ames.comercial.advantage.internal.ObtenirZonesTransportAds.ZonaTransportAds;
import ames.comercial.advantage.internal.response.*;
import ames.comercial.comandes.internal.application.command.ActualitzarComentariIntern;
import ames.comercial.comandes.request.StringRequest;
import ames.comercial.server.BeanUtils;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.SharedExceptions.ClientNoExisteix;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Path("/ads")
public class EndPointAds {
	
	@GET
	@Path("client")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<QueryClientResponse> obtenirClients(@QueryParam("filtre") String filtre,
			@QueryParam("responsables") String responsables,
			@DefaultValue("false") @QueryParam("inactius") boolean incloureInactius,
			@DefaultValue("") @QueryParam("colOrder") String colOrder,
			@DefaultValue("false") @QueryParam("orderAsc") boolean asc) {
		return new ObtenirClientsAds().query(filtre, responsables, incloureInactius, colOrder, asc);
	}

	@GET
	@Path("client/{codi}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ClientAds obtenirClient(@PathParam("codi") String client) {
		return new ObtenirClientAds().get(client).orElseThrow(() -> new ClientNoExisteix(client));
	}

	@PUT
	@Path("client/{codi}/notesclient")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void actualitzarNotesClient(@PathParam("codi") String client, StringRequest req) {
		new ActualitzarNotes().executarClient(client, req.valor());
	}

	@PUT
	@Path("client/{codi}/noteslogistica")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void actualitzarNotesLogistica(@PathParam("codi") String client, StringRequest req) {
		new ActualitzarNotes().executarLogistica(client, req.valor());
	}

	@PUT
	@Path("client/{codi}/notesmorositat")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void actualitzarNotesMorositat(@PathParam("codi") String client, StringRequest req) {
		new ActualitzarNotes().executarMorositat(client, req.valor());
	}

	@GET
	@Path("articleclient/{artint}/{clicod}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Optional<QueryArticleClientResponse> obtenirArticleClient(@PathParam("artint") String artint,
																	 @PathParam("clicod") String clicod) {
		return new ObtenirArticleClientAds().query(KeyArticleClient.of(artint, clicod));
	}

	@GET
	@Path("articleclientnorm/{client}/{artint}/{clicod}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Optional<ObtenirArticleClientNormalitzatsAdsResponse> obtenirArticleClientNorm(@PathParam("client") String client,
																				@PathParam("artint") String artint,
																				@PathParam("clicod") String clicod) {
		return new ObtenirArticleClientNormalitzatsAds().query(client, KeyArticleClient.of(artint, clicod));
	}

	@GET
	@Path("articleclient/{articleClient}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Optional<ArticleClientInformacioComandaResponse> obtenirArticleClient(@PathParam("articleClient") String articleClient) {
		return BeanUtils.getBean(IObtenirArticleClientInformacioComanda.class).executar(articleClient);
	}

	@GET
	@Path("articleclient/{articleClient}/altreInformacio")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public AltreInfoArticleClient ObtenirAltreInfoArticleClient(@PathParam("articleClient") String articleClient){
		return new ObtenirAltreInfoArticleClient().get(articleClient).orElseThrow(() -> new ClientNoExisteix(articleClient));
	}

	@GET
	@Path("client/{clicod}/altreInformacio")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public AltreInfoClient ObtenirAltreInfoClient(@PathParam("clicod") String clicod){
		return new ObtenirAltreInfoClient().get(clicod).orElseThrow(() -> new ClientNoExisteix(clicod));
	}

	@PUT
	@Path("articleclient/{articleclient}/notesembalatge")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void actualitzarNotesEmbalatge(@PathParam("articleclient") String articleclient, StringRequest req) {
		BeanUtils.getBean(ActualitzarNotesEmbalatge.class).executar(articleclient, req.valor());
	}
	
	@GET
	@Path("albaransfactures")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<QueryAlbaransFacturesByComandesResponse> obtenirArticlesClientsByClient(@QueryParam("comanda") int comanda, @QueryParam("linia") int linia) {
		return new ObtenirAlbaransFacturesByComandesAds().query(comanda, linia);
	}

	@GET
	@Path("facturesliniesmoviment")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<QueryFacturesLiniesMovimentResponse> obtenirFacturesLiniesMoviment(
			@QueryParam("albara") long albara,
			@QueryParam("empresa") String empresa,
			@QueryParam("clicod") String clicod,
			@QueryParam("artint") String artint) {
		return new ObtenirFacturesLiniesMovimentAds().query(albara, empresa, clicod, artint);
	}

	@GET
	@Path("zonestransport")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ZonaTransportAds> obtenirZonesTransport(@QueryParam("codiTransportista") String codiTransportista, @QueryParam("codiPais") String codiPais) {
		return BeanUtils.getBean(IObtenirZonesTransportAds.class).get(codiTransportista, codiPais);
	}

    @PUT
    @Path("client/{clicod}/comentariintern")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void actualitzarComentariInternClient(@PathParam("clicod") String clicod, StringRequest req) {
        BeanUtils.getBean(ActualitzarComentariIntern.class).executarClient(clicod, req.valor());
    }

    @PUT
    @Path("client/{clicod}/articleclient/{artint}/comentariintern")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void actualitzarComentariInternArticle(@PathParam("artint") String artint,
                                                  @PathParam("clicod") String clicod,
                                                  StringRequest req) {
        BeanUtils.getBean(ActualitzarComentariIntern.class).executarArticle(KeyArticleClient.of(artint, clicod), req.valor());
    }
	
}

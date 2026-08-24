package ames.comercial.comandes;

import ames.comercial.comandes.internal.application.command.*;
import ames.comercial.comandes.internal.application.query.*;
import ames.comercial.comandes.internal.application.query.BuscarComandes.BuscarComandesRequest;
import ames.comercial.comandes.internal.application.query.ObtenirComandaEspecial.ObtenirComandaEspecialResponse;
import ames.comercial.comandes.internal.application.query.ObtenirComandaNormalitzat.ObtenirComandaNormalitzatResponse;
import ames.comercial.comandes.internal.application.query.ObtenirInformacioReserves.ObtenirInformacioReservesResp;
import ames.comercial.comandes.internal.application.query.ObtenirLiniesAnulades.ObtenirLiniesAnuladesResponse;
import ames.comercial.comandes.internal.application.query.ObtenirStockSeguretat.ObtenirStockSeguretatResponse;
import ames.comercial.comandes.internal.domain.Adjunt;
import ames.comercial.comandes.internal.domain.linia.KeyLiniaComanda;
import ames.comercial.comandes.internal.domain.linia.TipusLiniaComanda;
import ames.comercial.comandes.internal.domain.service.RecalculReservesArticleclient;
import ames.comercial.comandes.request.*;
import ames.comercial.comandes.response.ItemHistoriaLiniaComanda;
import ames.comercial.comandes.response.ObtenirLiniesComandaResponse;
import ames.comercial.comandes.service.*;
import ames.comercial.comandes.service.IProviderTarifaActual.ProviderTarifaActualResponse;
import ames.comercial.comandes.service.request.CalculComandaNormalitzatRequest;
import ames.comercial.comandes.service.request.CalculCostTransportRequest;
import ames.comercial.comandes.service.request.CalculTarifaPesaRequest;
import ames.comercial.comandes.service.request.LiniaNormalitzatReq;
import ames.comercial.comandes.service.response.CalculComandaNormalitzatResponse;
import ames.comercial.comandes.service.response.CalculTarifaPesaResponse;
import ames.comercial.comandes.service.response.MissatgesCanvisComandaResponse;
import ames.comercial.costtransport.internal.CalcularCostTransport;
import ames.comercial.reserves.tasks.TascaCorreccioReservesArtfitArtcli;
import ames.comercial.reserves.tasks.TascaRecalculReserves;
import ames.comercial.server.BeanUtils;
import ames.comercial.server.Json;
import ames.comercial.shared.Empresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.TipusFormatDecimal;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/comanda")
public class EndPointComandes {
	
	private @Autowired ObjectMapper jsonMapper;

	@GET
	@Path("/tascareserves")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void tascaReserves() {
		BeanUtils.getBean(TascaRecalculReserves.class).executar();
	}

	@GET
	@Path("/tascacorreccioartfitartcli")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void tascaCorreccioArtfitArtcli() {
		BeanUtils.getBean(TascaCorreccioReservesArtfitArtcli.class).executar();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public String buscarComandes(@BeanParam BuscarComandesRequest request) {
		return BeanUtils.getBean(BuscarComandes.class).executar(request).toString();
	}

	@GET
	@Path("/datasortidaclient")
	@Produces(MediaType.TEXT_PLAIN)
	public String calcularDataSortidaClient(@QueryParam("client") String codiClient, @QueryParam("data") LocalDate data) {
		return BeanUtils.getBean(CalcularDiaSortidaClient.class).executar(codiClient, data).toString();
	}

	@GET
	@Path("/datasortida")
	@Produces(MediaType.TEXT_PLAIN)
	public String calcularDataSortida(@QueryParam("articleClient") String articleclient, @QueryParam("data") LocalDate data) {
		return BeanUtils.getBean(CalcularDiaSortidaArticleClient.class).executar(articleclient, data).toString();
	}

	@GET
	@Path("/datasortidaintermitja")
	@Produces(MediaType.TEXT_PLAIN)
	public String calcularDataSortidaIntermitja(@QueryParam("articleClient") String articleclient, @QueryParam("data") LocalDate data) {
		return BeanUtils.getBean(CalcularDataSortidaIntermitja.class).executar(articleclient, data).toString();
	}

	@GET
	@Path("/normalitzat/client/{client}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenirComandesNormalitzat(@PathParam("client") String client) {
		return BeanUtils.getBean(ObtenirComandesNormalitzat.class).executar(client).toString();
	}

	@GET
	@Path("/normalitzat/client/{client}/servides")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenirComandesNormalitzatServides(@PathParam("client") String client, @QueryParam("dataInici") LocalDate dataInici,
													 @QueryParam("dataFi") LocalDate dataFi, @QueryParam("comanda") String comanda) {
		return BeanUtils.getBean(ObtenirComandesNormalitzatServides.class).executar(client, dataInici, dataFi, comanda).toString();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String crearComanda(CrearComandaRequest req) {
		return BeanUtils.getBean(CrearComanda.class).executar(req);
	}

	@PUT
	@Path("/{idComanda}/adresa")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void canviarAdresa(@PathParam("idComanda") long idComanda, CanviarAdresaComandaRequest req) {
		BeanUtils.getBean(CanviarAdresaComanda.class).executar(idComanda, req);
	}

	@PUT
	@Path("/{comanda}/comentaris")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void actualitzarComentarisComanda(@PathParam("comanda") long comanda, StringRequest req) {
		BeanUtils.getBean(ActualitzarComentarisComanda.class).executar(comanda, req.valor());
	}

	@PUT
	@Path("/{comanda}/linia/{linia}/comentarisinterns")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void actualitzarComentarisInternsLiniaComanda(@PathParam("comanda") long comanda, @PathParam("linia") long linia, StringRequest req) {
		BeanUtils.getBean(ActualitzarComentarisInternsLiniaComanda.class).executar(KeyLiniaComanda.of(comanda,linia), req.valor());
	}

	@PUT
	@Path("/{comanda}/linia/{linia}/comentarisclient")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void actualitzarComentarisClientLiniaComanda(@PathParam("comanda") long comanda, @PathParam("linia") long linia, StringRequest req) {
		BeanUtils.getBean(ActualitzarComentarisClientLiniaComanda.class).executar(KeyLiniaComanda.of(comanda,linia), req.valor());
	}

	@GET
	@Path("/{idComanda}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObtenirComandaEspecialResponse obtenirComandaEspecial(@PathParam("idComanda") long idComanda) {
		return BeanUtils.getBean(ObtenirComandaEspecial.class).executar(idComanda);
	}

	@GET
	@Path("/perComandaClient")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Optional<ObtenirComandaEspecialResponse> obtenirComandaEspecialPerComandaClient(@QueryParam("client") String client, @QueryParam("comanda") String comandaClient) {
		return BeanUtils.getBean(ObtenirComandaEspecialPerComandaClient.class).executar(client, comandaClient);
	}

	@GET
	@Path("/normalitzat/{idComanda}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObtenirComandaNormalitzatResponse obtenirComandaNormalitzat(@PathParam("idComanda") long idComanda) {
		return BeanUtils.getBean(ObtenirComandaNormalitzat.class).executar(idComanda);
	}

	@GET
	@Path("/normalitzat/perComandaClient")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Optional<ObtenirComandaNormalitzatResponse> obtenirComandaNormalitzatPerComandaClient(@QueryParam("client") String client, @QueryParam("comanda") String comandaClient,
																								 @QueryParam("empresa") String empresa) {
		return BeanUtils.getBean(ObtenirComandaNormalitzatPerComandaClient.class).executar(client, comandaClient, Empresa.getByClau(empresa));
	}

	@GET
	@Path("/normalitzat/{idComanda}/justificant")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response generarJustificantRecepcio(@PathParam("idComanda") long idComanda,
														 @QueryParam("is_distribuidor") @DefaultValue("true") boolean isDistribuidor,
														 @QueryParam("idioma") @DefaultValue("en") String idioma,
														 @QueryParam("format_decimal") @DefaultValue("COMA_PUNT") TipusFormatDecimal tipusFormatDecimal) {
		var bytes = BeanUtils.getBean(GenerarPdfJustificantComandaNormalitzat.class).run(idComanda, isDistribuidor, idioma, tipusFormatDecimal);
		var fileName = String.format("justificant_%d", idComanda);
		return Response.ok(bytes)
				.type("application/pdf")
				.header("Content-Disposition", "filename=\"" + fileName + "\"")
				.build();
	}

	@GET
	@Path("/especials/{idComanda}/justificant")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response generarJustificantRecepcioEspecials(@PathParam("idComanda") long idComanda,
													   	@QueryParam("idioma") @DefaultValue("en") String idioma,
													   	@QueryParam("format_decimal") @DefaultValue("COMA_PUNT") TipusFormatDecimal tipusFormatDecimal,
														@QueryParam("tipus") Optional<TipusLiniaComanda> tipusComanda) {
		var bytes = BeanUtils.getBean(GenerarPdfJustificantComandaEspecial.class).run(idComanda, idioma, tipusFormatDecimal, tipusComanda);
		var fileName = String.format("justificant_%d", idComanda);
		return Response.ok(bytes)
				.type("application/pdf")
				.header("Content-Disposition", "filename=\"" + fileName + "\"")
				.build();
	}

	@POST
	@Path("/especials/{idComanda}/justificant/enviar")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/json")
	public void enviarJustificantRecepcioEspecial(
			@PathParam("idComanda") long idComanda,
			@FormDataParam("files") List<FormDataBodyPart> files,
			@FormDataParam("request") FormDataBodyPart request) {

		//Deserialitzem el contingut del request per a poder llegir-lo i enviar-lo per correu
		//Abans es passava a través del header, perÒ això no deixava enviar caràcters especials.
		var params = new Json(jsonMapper).deserialize(request.getValue(), EnviarJustificantRecepecioEspecialRequest.class);

		Map<String, InputStream> fitxersAdjunts = new HashMap<>();
		if (files != null) {
			for (FormDataBodyPart filePart : files) {
				String fileName = filePart.getContentDisposition().getFileName();
				InputStream fileInputStream = filePart.getEntityAs(InputStream.class);
				fitxersAdjunts.put(fileName, fileInputStream);
			}
		}
		BeanUtils.getBean(EnviarJustificantRecepcioComandaEspecial.class).executar(idComanda, params, fitxersAdjunts);
	}

	@GET
	@Path("/normalitzat/{idComanda}/justificant/enviat")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response obtenirJustificantRecepcioEnviat(@PathParam("idComanda") long idComanda) {
		var file = BeanUtils.getBean(ObtenirPdfJustificantEnviat.class).run(idComanda);
		var fileName = String.format("justificant_%d", idComanda);
		return Response.ok(file)
				.type("application/pdf")
				.header("Content-Disposition", "filename=\"" + fileName + "\"")
				.build();
	}

	@POST
	@Path("/normalitzat/{idComanda}/justificant/enviar")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces("application/json")
	public void enviarJustificantRecepcio(
			@PathParam("idComanda") long idComanda,
			@FormDataParam("files") List<FormDataBodyPart> files,
			@FormDataParam("request") FormDataBodyPart request) {

		//Deserialitzem el contingut del request per a poder llegir-lo i enviar-lo per correu
		//Abans es passava a través del header, perÒ això no deixava enviar caràcters especials.
		var params = new Json(jsonMapper).deserialize(request.getValue(), EnviarJustificantRecepecioRequest.class);

		Map<String, InputStream> fitxersAdjunts = new HashMap<>();
		if (files != null) {
			for (FormDataBodyPart filePart : files) {
				String fileName = filePart.getContentDisposition().getFileName();
				InputStream fileInputStream = filePart.getEntityAs(InputStream.class);
				fitxersAdjunts.put(fileName, fileInputStream);
			}
		}
		BeanUtils.getBean(EnviarJustificantRecepcioComanda.class).executar(idComanda, params, fitxersAdjunts);
	}

	@POST
	@Path("/normalitzat")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public CrearComandaNormalitzatResponse crearComandaNormalitzat(CrearComandaNormalitzatRequest req) {
		return BeanUtils.getBean(CrearComandaNormalitzat.class).executar(req);
	}

	@DELETE
	@Path("/normalitzat/{idComanda}/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MissatgesCanvisComandaResponse cancelarComandaNormalitzat(@PathParam("idComanda") long idComanda) {
		return BeanUtils.getBean(CancelarComandaNormalitzat.class).executar(idComanda);
	}

	@POST
	@Path("/normalitzat/{idComanda}/linia")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MissatgesCanvisComandaResponse crearLiniaComandaNormalitzat(@PathParam("idComanda") long idComanda, LiniaNormalitzatReq req) {
		return BeanUtils.getBean(AfegirLiniaComandaNormalitzat.class).executar(idComanda, req);
	}

	@PUT
	@Path("/normalitzat/{idComanda}/linia/{linia}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MissatgesCanvisComandaResponse modificarLiniaComandaNormalitzat(@PathParam("idComanda") long idComanda, @PathParam("linia") long linia,
														 LiniaNormalitzatReq req) {
		return BeanUtils.getBean(EditarLiniaComandaNormalitzat.class).executar(KeyLiniaComanda.of(idComanda, linia), req);
	}

    @PUT
    @Path("/normalitzat/{idComanda}/nom")
    @Consumes(MediaType.TEXT_PLAIN)
    public void canviarNomComandaNormalitzat(@PathParam("idComanda") long idComanda, String nomNou) {
        BeanUtils.getBean(CanviarNomComandaNormalitzat.class).executar(idComanda, nomNou);
    }

	@PUT
	@Path("/normalitzat/{idComanda}/linia/{linia}/fixar")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void fixarPreuLiniaNormalitzat(@PathParam("idComanda") long idComanda, @PathParam("linia") long linia,
										  FixarPreuRequest req) {
		BeanUtils.getBean(FixarPreuLiniaComandaNormalitzat.class).executar(KeyLiniaComanda.of(idComanda, linia), req.valor());
	}

	@PUT
	@Path("/normalitzat/{idComanda}/linia/{linia}/desfixar")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void desfixarPreuLiniaNormalitzat(@PathParam("idComanda") long idComanda, @PathParam("linia") long linia) {
		BeanUtils.getBean(DesfixarPreuLiniaComandaNormalitzat.class).executar(KeyLiniaComanda.of(idComanda, linia));
	}

	@DELETE
	@Path("/normalitzat/{idComanda}/linia/{linia}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public MissatgesCanvisComandaResponse eliminarLiniaComandaNormalitzat(@PathParam("idComanda") long idComanda, @PathParam("linia") long linia) {
		return BeanUtils.getBean(EliminarLiniaComandaNormalitzat.class).executar(KeyLiniaComanda.of(idComanda, linia));
	}

	@GET
	@Path("/{comanda}/adjunts")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Adjunt> getAdjunts(@PathParam("comanda") long comanda) {
		return BeanUtils.getBean(ObtenirAdjuntsComanda.class).executar(comanda);
	}

	@POST
	@Path("/{comanda}/adjunts")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void pujarAdjunt(@PathParam("comanda") long comanda, @FormDataParam("file") InputStream inputStream,
							@FormDataParam("file") FormDataContentDisposition fileDetail) {
		String nomFitxer = new String(fileDetail.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		BeanUtils.getBean(AfegirAdjuntComanda.class).executar(comanda, inputStream, nomFitxer);
	}

	@PUT
	@Path("/{comanda}/adjunts/{codiAdjunt}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void renombrarAdjunt(@PathParam("comanda") long comanda, @PathParam("codiAdjunt") String codiAdjunt, StringRequest req) {
		BeanUtils.getBean(RenombrarAdjuntComanda.class).executar(comanda, codiAdjunt, req.valor());
	}

	@DELETE
	@Path("/{comanda}/adjunts/{codiAdjunt}")
	public void eliminarAdjunt(@PathParam("comanda") long comanda, @PathParam("codiAdjunt") String codiAdjunt) {
		BeanUtils.getBean(EliminarAdjuntComanda.class).executar(comanda, codiAdjunt);
	}

	@GET
	@Path("/{comanda}/adjunts/{codiAdjunt}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response descarregarAdjunt(@PathParam("comanda") long comanda, @PathParam("codiAdjunt") String codiAdjunt) {
		var nomFile = BeanUtils.getBean(ObtenirAdjuntComanda.class).executar(comanda, codiAdjunt).nom();
		var file = BeanUtils.getBean(ObtenirAdjuntComandaFile.class).executar(comanda, codiAdjunt);
		return Response.status(200)
				.entity(file)
				.header("Content-Disposition", "attachment; filename=" + nomFile)
				.build();
	}

	@POST
	@Path("/{comanda}/traspas")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public long traspassarComanda(@PathParam("comanda") long comanda, TraspassarComandaRequest req) {
		return BeanUtils.getBean(TraspassarComanda.class).executar(comanda, req);
	}

	@POST
	@Path("/{comanda}/linia")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String crearLiniaComanda(@PathParam("comanda") long comanda, LiniaComandaRequest req) {
		return BeanUtils.getBean(CrearLiniaComanda.class).executar(comanda, req);
	}
	
	@GET
	@Path("/{comanda}/linia/{linia}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ItemHistoriaLiniaComanda> obtenirHistoricLiniaComanda(@PathParam("comanda") long comanda, @PathParam("linia") long linia) {
		return BeanUtils.getBean(ObtenirHistoricLinia.class).executar(comanda, linia);
	}

	@GET
	@Path("/programa/{client}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String obtenirComandesEspecialsPerClient(@PathParam("client") String client, @QueryParam("dataInici") LocalDate dataInici,
													 @QueryParam("dataFi") LocalDate dataFi, @QueryParam("comanda") String comanda,
													@QueryParam("includeServides") boolean includeServides) {
		return BeanUtils.getBean(ObtenirComandesEspecials.class).executar(client, dataInici, dataFi, comanda, includeServides).toString();
	}
	
	@PUT
	@Path("/{comanda}/linia/{linia}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String modificarLiniaComanda(@PathParam("comanda") long comanda, @PathParam("linia") long linia, LiniaComandaRequest req) {
		return BeanUtils.getBean(ModificarLiniaComanda.class).executar(comanda, linia, req);
	}

	@DELETE
	@Path("/{comanda}/linia/{linia}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void anularLiniaComanda(@PathParam("comanda") long comanda, @PathParam("linia") long linia) {
		BeanUtils.getBean(AnularLiniaComanda.class).executar(KeyLiniaComanda.of(comanda, linia));
	}

	@PUT
	@Path("/{comanda}/linia/{linia}/kanban")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String kanbanitzarLiniaComanda(@PathParam("comanda") long comanda, @PathParam("linia") long linia, KanbanitzarLiniaComandaRequest req) {
		return BeanUtils.getBean(KanbanitzarLiniaComanda.class).executar(KeyLiniaComanda.of(comanda, linia), req);
	}

	@PUT
	@Path("/{comanda}/linia/{linia}/fermorientatiu")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void kanbanitzarLiniaComanda(@PathParam("comanda") long comanda, @PathParam("linia") long linia) {
		BeanUtils.getBean(CanviarFermOrientatiuLiniaComanda.class).executar(KeyLiniaComanda.of(comanda, linia));
	}

	@PUT
	@Path("/{comanda}/linia/{linia}/reserva/solicitar")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void solicitarReservaLiniaComanda(@PathParam("comanda") long comanda, @PathParam("linia") long linia) {
		BeanUtils.getBean(SolicitarReservaLiniaComanda.class).executar(KeyLiniaComanda.of(comanda, linia));
	}

	@GET
	@Path("/linia")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObtenirLiniesComandaResponse liniesComanda(@QueryParam("dataInicial") LocalDate dataInicial, @QueryParam("dataFinal") LocalDate dataFinal,
													  @QueryParam("articleClient") String articleClient,
													  @QueryParam("ocultarServides") @DefaultValue("false") boolean ocultarServides,
													  @QueryParam("mostrarOrdreAscendent") @DefaultValue("true") boolean mostrarOrdreAscendent) {
		return BeanUtils.getBean(ObtenirLiniesComanda.class).executar(articleClient, dataInicial, dataFinal, ocultarServides, mostrarOrdreAscendent);
	}

	@GET
	@Path("/linia/anulades")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<ObtenirLiniesAnuladesResponse> liniesAnulades(@QueryParam("dataInicial") LocalDate dataInicial, @QueryParam("dataFinal") LocalDate dataFinal,
															  @QueryParam("comanda") String comanda,
															  @QueryParam("articleClient") String articleClient) {
		return BeanUtils.getBean(ObtenirLiniesAnulades.class).executar(articleClient, comanda, dataInicial, dataFinal);
	}
	
	@GET
	@Path("/calculadora")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public CalculComandaNormalitzatResponse liniesComanda(@HeaderParam("params") String jsonParams) {
		var params = new Json(jsonMapper).deserialize(jsonParams, CalculComandaNormalitzatRequest.class);
		return BeanUtils.getBean(CalculadoraComandaNormalitzat.class).calcula(params.codiClient(), params.linies());
	}

	@GET
	@Path("/calculadora/tarifa")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public CalculTarifaPesaResponse liniesTarifes(@HeaderParam("params") String jsonParams) {
		var params = new Json(jsonMapper).deserialize(jsonParams, CalculTarifaPesaRequest.class);
		return BeanUtils.getBean(CalculadoraTarifaPesa.class).calcula(params.codiClient(), params.linies(), params.importTotalNet(),
				params.importTotalBrut(), params.pesTotal(), params.incrementPesTransport());
	}

    @GET
    @Path("/calculadora/transport")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<BigDecimal> calcularCostTransport(@HeaderParam("params") String jsonParams) {
        var params = new Json(jsonMapper).deserialize(jsonParams, CalculCostTransportRequest.class);
        return BeanUtils.getBean(CalcularCostTransport.class).calcula(params.codiClient(), params.importTotal(), params.pesTotal());
    }

	@POST
	@Path("/calculadora/fitxer/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public CalculComandaNormalitzatResponse pujarAdjunt (@FormDataParam("file") InputStream inputStream,
							 @FormDataParam("file") FormDataContentDisposition fileDetail,
							 @HeaderParam("params") String jsonParams) throws IOException {
		String nomFitxer = new String (fileDetail.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		var params = new Json(jsonMapper).deserialize(jsonParams, CalculComandaNormalitzatRequest.class);
		if (nomFitxer.endsWith(".csv"))
			return BeanUtils.getBean(CalculadoraComandaNormalitzatFitxerCsv.class).calcula(params.codiClient(), inputStream);
		return BeanUtils.getBean(CalculadoraComandaNormalitzatFitxer.class).calcula(params.codiClient(), inputStream);
	}

	@GET
	@Path("/reserva/{artint}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObtenirInformacioReservesResp obtenirInformacioReserves(@PathParam("artint") String artint, @QueryParam("empresa") String empresa) {
		return BeanUtils.getBean(ObtenirInformacioReserves.class).executar(artint, empresa);
	}

	@GET
	@Path("/reserva/{artint}/recalcula")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void recalcularReserves(@PathParam("artint") String artint, @QueryParam("empresa") String empresa) {
		BeanUtils.getBean(RecalculReservesArticleclient.class).executar(KeyArticleClient.of(artint, "000000"), Empresa.getByClau(empresa));
	}

	@GET
	@Path("/tarifaactual/{clicod}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ProviderTarifaActualResponse obtenirTarifaActual(@PathParam("clicod") String clicod) {
		return BeanUtils.getBean(IProviderTarifaActual.class).executar(clicod);
	}

	@GET
	@Path("/{articleclient}/stockseguretat")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Optional<ObtenirStockSeguretatResponse> obtenirStockSeguretat(@PathParam("articleclient") String articleClientParam) {
		return BeanUtils.getBean(ObtenirStockSeguretat.class).executar(articleClientParam);
	}

	@POST
	@Path("/{articleclient}/stockseguretat")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String definirStockSeguretat(@PathParam("articleclient") String articleClientParam, DefinirStockSeguretatRequest request) {
		return BeanUtils.getBean(DefinirStockSeguretat.class).executar(articleClientParam, request);
	}

	@GET
	@Path("/especials/{comanda}/exportarDetall")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response exportarDetallComanda (@PathParam("comanda") int comanda,
										   @QueryParam("dataInici") LocalDate dataInici,
										   @QueryParam("dataFi") LocalDate dataFi,
										   @QueryParam("mostrarEliminades") boolean mostrarEliminades) throws IOException {
		byte[] bytes = BeanUtils.getBean(ExportarDetallComanda.class).exportar(comanda, dataInici, dataFi, mostrarEliminades);
		return Response.ok(bytes)
				.header("Content-Disposition", "attachment;filename=DetallComanda.xlsx")
				.build();
	}

	@GET
	@Path("/especials/{client}/exportarDetallsClient")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response exportarDetallComandesClient (@PathParam("client") String client,
										   @QueryParam("dataInici") LocalDate dataInici,
										   @QueryParam("dataFi") LocalDate dataFi,
										   @QueryParam("mostrarEliminades") boolean mostrarEliminades) throws IOException {
		byte[] bytes = BeanUtils.getBean(ExportarDetallComandesClient.class).exportar(client, dataInici, dataFi, mostrarEliminades);
		return Response.ok(bytes)
				.header("Content-Disposition", "attachment;filename=DetallComanda.xlsx")
				.build();
	}

	@GET
	@Path("/especials/{article}/exportarDetallsArticle")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response exportarDetallComandesArticle (@PathParam("article") String article,
											      @QueryParam("client") String client,
												  @QueryParam("dataInici") LocalDate dataInici,
												  @QueryParam("dataFi") LocalDate dataFi,
												  @QueryParam("mostrarEliminades") boolean mostrarEliminades) throws IOException {
		byte[] bytes = BeanUtils.getBean(ExportarDetallComandesArticle.class).exportar(article, client, dataInici, dataFi, mostrarEliminades);
		return Response.ok(bytes)
				.header("Content-Disposition", "attachment;filename=DetallComanda.xlsx")
				.build();
	}
}

package ames.comercial.albarans;

import ames.comercial.albarans.internal.application.command.*;
import ames.comercial.albarans.internal.application.command.AfegirLiniaConsum.PecaAfegirConsumRequest;
import ames.comercial.albarans.internal.application.command.AfegirLiniaTraspas.AfegirLiniaTraspasRequest;
import ames.comercial.albarans.internal.application.command.CanviarAlbaraEspecial.CanviarAlbaraEspecialRequest;
import ames.comercial.albarans.internal.application.command.CanviarDataAlbara.CanviarDataAlbaraRequest;
import ames.comercial.albarans.internal.application.command.CanviarFacturacioAutomaticaAlbara.CanviarFacturacioAutomaticaAlbaraRequest;
import ames.comercial.albarans.internal.application.command.CanviarQuantitatPendentFacturarLinia.CanviarQuantitatPendentFacturarLiniaRequest;
import ames.comercial.albarans.internal.application.command.CanviarTraspasAbonable.CanviarTraspasAbonableRequest;
import ames.comercial.albarans.internal.application.command.CrearAlbara.CrearAlbaraRequest;
import ames.comercial.albarans.internal.application.command.CrearAlbaraTraspas.CrearAlbaraTraspasRequest;
import ames.comercial.albarans.internal.application.command.CrearAlbaraTraspasManual.CrearAlbaraTraspasManualRequest;
import ames.comercial.albarans.internal.application.command.CrearAlbaraTraspasManual.CrearAlbaraTraspasManualResponse;
import ames.comercial.albarans.internal.application.command.CrearCapsaleraConsum.CrearCapsaleraConsumRequest;
import ames.comercial.albarans.internal.application.command.CrearAlbaraTraspasPlataforma.CrearAlbaraTraspasPlataformaRequest;
import ames.comercial.albarans.internal.application.query.*;
import ames.comercial.albarans.internal.application.query.BuscarAlbarans.BuscarAlbaransRequest;
import ames.comercial.albarans.internal.application.query.BuscarAlbarans.BuscarAlbaransResponse;
import ames.comercial.albarans.internal.application.query.ComprovarAlbaraEspecialClient.AlbaraMateixEspecialResponse;
import ames.comercial.albarans.internal.application.query.ObtenirAlbaransPendentConsum.AlbaraPendentConsumResponse;
import ames.comercial.albarans.internal.application.query.ObtenirClientsPlataforma.ClientPlataformaResponse;
import ames.comercial.albarans.internal.application.query.ObtenirDetallAlbara.DetallAlbaraResponse;
import ames.comercial.albarans.internal.application.query.ObtenirMagatzemsPlataforma.PlataformaResponse;
import ames.comercial.albarans.internal.application.query.ObtenirResumAlbaransOberts.ResumAlbaransObertsResponse;
import ames.comercial.albarans.internal.application.query.ObtenirStockPlataforma.PecaStockPlataformaResponse;
import ames.comercial.albarans.internal.application.query.ObtenirTraspassosConsumits.TraspasConsumitResponse;
import ames.comercial.albarans.internal.application.query.ObtenirUltimsAlbarans.ObtenirUltimsAlbaransResponse;
import ames.comercial.albarans.internal.application.query.PrevisualitzarAltaLiniaTraspas.PreparacioLiniaTraspasResponse;
import ames.comercial.albarans.internal.application.query.PrevisualitzarAltaLiniaTraspas.ValidacioLiniaTraspasResponse;
import ames.comercial.albarans.internal.application.query.PrevisualitzarCreacioAlbara.PreviewCrearAlbaraRequest;
import ames.comercial.albarans.internal.application.query.PrevisualitzarCreacioAlbara.PreviewCrearAlbaraResponse;
import ames.comercial.albarans.internal.application.query.PrevisualitzarCreacioAlbaraTraspas.PreviewCrearAlbaraTraspasRequest;
import ames.comercial.albarans.internal.application.query.PrevisualitzarCreacioAlbaraTraspas.PreviewCrearAlbaraTraspasResponse;
import ames.comercial.albarans.internal.domain.Adjunt;
import ames.comercial.albarans.internal.domain.albara.Albara;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.albarans.internal.domain.linia.KeyLiniaAlbara;
import ames.comercial.albarans.request.*;
import ames.comercial.server.BeanUtils;
import ames.comercial.shared.Adresa;
import ames.comercial.shared.KeyArticleClient;
import ames.comercial.shared.TipusFormatDecimal;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Path("/albara")
public class EndPointAlbarans {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<BuscarAlbaransResponse> buscarAlbarans(@BeanParam BuscarAlbaransRequest request) {
		return BeanUtils.getBean(BuscarAlbarans.class).executar(request);
	}

	@GET
	@Path("/{codi}/{empresa}/detall")
	@Produces(MediaType.APPLICATION_JSON)
	public DetallAlbaraResponse obtenirDetallAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa) {
		var idAlbara = KeyAlbara.of(codi, empresa);
		return BeanUtils.getBean(ObtenirDetallAlbara.class).executar(idAlbara);
	}

	@GET
	@Path("/{clicod}/{artint}/ultims/")
	@Produces(MediaType.APPLICATION_JSON)
	public ObtenirUltimsAlbaransResponse buscarAlbarans(@PathParam("clicod") String clicod, @PathParam("artint") String artint,
														@QueryParam("codiAlbaraFacturaReferenciaTransit") String codiAlbaraFacturaReferenciaTransit,
														@QueryParam("acumulatSegonsClient") long acumulatSegonsClient) {
		return BeanUtils.getBean(ObtenirUltimsAlbarans.class).executar(KeyArticleClient.of(artint, clicod), codiAlbaraFacturaReferenciaTransit, acumulatSegonsClient, 100);
	}

	@GET
	@Path("/{clicod}/oberts")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ResumAlbaransObertsResponse obtenirResumAlabransOberts(String codiClient) {
		return BeanUtils.getBean(ObtenirResumAlbaransOberts.class).executar(codiClient);
	}

	@POST
	@Path("/calcular")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public PreviewCrearAlbaraResponse calcularCreacioAlbara(PreviewCrearAlbaraRequest req) {
		return BeanUtils.getBean(PrevisualitzarCreacioAlbara.class).executar(req);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ResultatCreacioAlbaransResponse crearAlbara(CrearAlbaraRequest req) {
		return BeanUtils.getBean(CrearAlbara.class).executar(req);
	}

	@POST
	@Path("/traspas/calcular")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public PreviewCrearAlbaraTraspasResponse calcularCreacioAlbaraTraspas(PreviewCrearAlbaraTraspasRequest req) {
		return BeanUtils.getBean(PrevisualitzarCreacioAlbaraTraspas.class).executar(req, false);
	}

	@POST
	@Path("/plataforma/calcular")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public PreviewCrearAlbaraTraspasResponse calcularCreacioAlbaraTraspasPlataforma(PreviewCrearAlbaraTraspasRequest req) {
		return BeanUtils.getBean(PrevisualitzarCreacioAlbaraTraspas.class).executar(req, true);
	}

	@POST
	@Path("/plataforma")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ResultatCreacioAlbaransResponse crearAlbaraPlataforma(CrearAlbaraTraspasPlataformaRequest req) {
		return BeanUtils.getBean(CrearAlbaraTraspasPlataforma.class).executar(req);
	}

	@POST
	@Path("/traspas")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ResultatCreacioAlbaransResponse crearAlbaraTraspas(CrearAlbaraTraspasRequest req) {
		return BeanUtils.getBean(CrearAlbaraTraspas.class).executar(req);
	}

	// --- Alta manual d'albarans de traspàs ---

	@POST
	@Path("/traspas/manual")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public CrearAlbaraTraspasManualResponse crearAlbaraTraspasManual(CrearAlbaraTraspasManualRequest req) {
		return BeanUtils.getBean(CrearAlbaraTraspasManual.class).executar(req);
	}

	@GET
	@Path("/{codi}/{empresa}/linia/traspas/preparar")
	@Produces(MediaType.APPLICATION_JSON)
	public PreparacioLiniaTraspasResponse prepararLiniaTraspas(@PathParam("codi") Long codi, @PathParam("empresa") String empresa,
			@QueryParam("artint") String artint, @QueryParam("clicod") String clicod) {
		return BeanUtils.getBean(PrevisualitzarAltaLiniaTraspas.class)
				.preparar(KeyAlbara.of(codi, empresa), KeyArticleClient.of(artint, clicod));
	}

	@GET
	@Path("/{codi}/{empresa}/linia/traspas/validar")
	@Produces(MediaType.APPLICATION_JSON)
	public ValidacioLiniaTraspasResponse validarLiniaTraspas(@PathParam("codi") Long codi, @PathParam("empresa") String empresa,
			@QueryParam("artint") String artint, @QueryParam("clicod") String clicod,
			@QueryParam("quantitat") long quantitat) {
		return BeanUtils.getBean(PrevisualitzarAltaLiniaTraspas.class)
				.validar(KeyAlbara.of(codi, empresa), KeyArticleClient.of(artint, clicod), quantitat);
	}

	@POST
	@Path("/{codi}/{empresa}/linia/traspas")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public KeyLiniaAlbara afegirLiniaTraspas(@PathParam("codi") Long codi, @PathParam("empresa") String empresa,
			AfegirLiniaTraspasRequest req) {
		return BeanUtils.getBean(AfegirLiniaTraspas.class).executar(KeyAlbara.of(codi, empresa), req);
	}

	// --- Consums de magatzems plataforma ---

	@GET
	@Path("/consum/plataformes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PlataformaResponse> obtenirMagatzemsPlataforma() {
		return BeanUtils.getBean(ObtenirMagatzemsPlataforma.class).executar();
	}

	@GET
	@Path("/consum/clients/{magatzem}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<ClientPlataformaResponse> obtenirClientsPlataforma(@PathParam("magatzem") String magatzem) {
		return BeanUtils.getBean(ObtenirClientsPlataforma.class).executar(magatzem);
	}

	@GET
	@Path("/consum/stock/{magatzem}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PecaStockPlataformaResponse> obtenirStockPlataforma(@PathParam("magatzem") String magatzem) {
		return BeanUtils.getBean(ObtenirStockPlataforma.class).executar(magatzem);
	}

	@GET
	@Path("/consum/pendent/{empresa}/{magatzem}/{artint}/{clicod}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AlbaraPendentConsumResponse> obtenirAlbaransPendentConsum(@PathParam("empresa") String empresa,
			@PathParam("magatzem") String magatzem, @PathParam("artint") String artint, @PathParam("clicod") String clicod) {
		return BeanUtils.getBean(ObtenirAlbaransPendentConsum.class).executar(magatzem, empresa, KeyArticleClient.of(artint, clicod));
	}

	@GET
	@Path("/consum/{empresa}/{codi}/{linia}/traspassos")
	@Produces(MediaType.APPLICATION_JSON)
	public List<TraspasConsumitResponse> obtenirTraspassosConsumits(@PathParam("empresa") String empresa,
			@PathParam("codi") long codi, @PathParam("linia") long linia) {
		return BeanUtils.getBean(ObtenirTraspassosConsumits.class).executar(KeyLiniaAlbara.of(empresa, codi, linia));
	}

	@POST
	@Path("/consum/capsalera")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public KeyAlbara crearCapsaleraConsum(CrearCapsaleraConsumRequest req) {
		return BeanUtils.getBean(CrearCapsaleraConsum.class).executar(req);
	}

	@POST
	@Path("/consum/{empresa}/{codi}/linia")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void afegirLiniaConsum(@PathParam("empresa") String empresa, @PathParam("codi") Long codi, PecaAfegirConsumRequest req) {
		BeanUtils.getBean(AfegirLiniaConsum.class).executar(KeyAlbara.of(codi, empresa), req);
	}

	@DELETE
	@Path("/{codi}/{empresa}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void eliminarAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa) {
		var idAlbara = KeyAlbara.of(codi, empresa);
		BeanUtils.getBean(EliminarAlbara.class).executar(idAlbara);
	}

	@DELETE
	@Path("/{codi}/{empresa}/linia/{linia}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void eliminarLiniaAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa, @PathParam("linia") long linia) {
		var idLinia = KeyLiniaAlbara.of(empresa, codi, linia);
		BeanUtils.getBean(EliminarLiniaAlbara.class).executar(idLinia);
	}

	@PUT
	@Path("/{codi}/{empresa}/tancar")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void tancarAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa) {
		var idAlbara = KeyAlbara.of(codi, empresa);
		BeanUtils.getBean(TancarAlbara.class).executar(idAlbara);
	}

	@PUT
	@Path("/{codi}/{empresa}/reobrir")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void reobrirAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa) {
		var idAlbara = KeyAlbara.of(codi, empresa);
		BeanUtils.getBean(ReobrirAlbara.class).executar(idAlbara);
	}

	@PUT
	@Path("/{codi}/{empresa}/nota")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void guardarNotaAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa, NotaAlbaraRequest req) {
		BeanUtils.getBean(GuardarNota.class).executar(codi, empresa, req.artint(), req.clicod(), req.nota());
	}

	/**
	 * Albarans del client (de qualsevol tipus) que ja tenen aquest número d'albarà especial. Serveix
	 * per avisar abans de crear o modificar-ne un; la llista buida vol dir que no hi ha coincidències.
	 */
	@GET
	@Path("/albara-especial/coincidencies")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AlbaraMateixEspecialResponse> comprovarAlbaraEspecial(@QueryParam("empresa") String empresa,
			@QueryParam("client") String client, @QueryParam("valor") String valor,
			@QueryParam("codiExclos") Long codiExclos) {
		return BeanUtils.getBean(ComprovarAlbaraEspecialClient.class).executar(empresa, client, valor, codiExclos);
	}

	@PUT
	@Path("/{codi}/{empresa}/albara-especial")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void canviarAlbaraEspecial(@PathParam("codi") Long codi, @PathParam("empresa") String empresa, CanviarAlbaraEspecialRequest req) {
		BeanUtils.getBean(CanviarAlbaraEspecial.class).executar(KeyAlbara.of(codi, empresa), req);
	}

	@PUT
	@Path("/{codi}/{empresa}/facturacio-automatica")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void canviarFacturacioAutomaticaAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa, CanviarFacturacioAutomaticaAlbaraRequest req) {
		BeanUtils.getBean(CanviarFacturacioAutomaticaAlbara.class).executar(KeyAlbara.of(codi, empresa), req);
	}

	@PUT
	@Path("/{codi}/{empresa}/traspas-abonable")
	@Consumes(MediaType.APPLICATION_JSON)
	public void canviarTraspasAbonable(@PathParam("codi") Long codi, @PathParam("empresa") String empresa, CanviarTraspasAbonableRequest req) {
		BeanUtils.getBean(CanviarTraspasAbonable.class).executar(KeyAlbara.of(codi, empresa), req);
	}

	@PUT
	@Path("/{codi}/{empresa}/linia/{linia}/pendent-facturar")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void canviarQuantitatPendentFacturarLinia(@PathParam("codi") Long codi, @PathParam("empresa") String empresa,
			@PathParam("linia") long linia, CanviarQuantitatPendentFacturarLiniaRequest req) {
		var idLinia = KeyLiniaAlbara.of(empresa, codi, linia);
		BeanUtils.getBean(CanviarQuantitatPendentFacturarLinia.class).executar(idLinia, req);
	}

	@PUT
	@Path("/{codi}/{empresa}/data")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void canviarDataAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa, CanviarDataAlbaraRequest req) {
		BeanUtils.getBean(CanviarDataAlbara.class).executar(KeyAlbara.of(codi, empresa), req);
	}

	@PUT
	@Path("/{codi}/{empresa}/adresa")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void canviarAdresaAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa, CanviarAdresaAlbaraRequest req) {
		var idAlbara = KeyAlbara.of(codi, empresa);
		BeanUtils.getBean(CanviarAdresaAlbara.class).executar(idAlbara, req);
	}

	@PUT
	@Path("/{codi}/{empresa}/adresa-broker")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void canviarAdresaBrokerAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa, Adresa req) {
		var idAlbara = KeyAlbara.of(codi, empresa);
		BeanUtils.getBean(CanviarAdresaAlbara.class).executarBroker(idAlbara, req);
	}

	@PUT
	@Path("/{codi}/{empresa}/adresa-proforma")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void canviarAdresaProformaAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa, Adresa req) {
		var idAlbara = KeyAlbara.of(codi, empresa);
		BeanUtils.getBean(CanviarAdresaAlbara.class).executarProforma(idAlbara, req);
	}

	@GET
	@Path("/{clicod}/{artint}/exportar")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response exportarAlbarans (@PathParam("clicod") String clicod, @PathParam("artint") String artint,
									  @QueryParam("codiAlbaraFacturaReferenciaTransit") String codiAlbaraFacturaReferenciaTransit,
									  @QueryParam("acumulatSegonsClient") long acumulatSegonsClient) throws IOException {
		byte[] bytes = BeanUtils.getBean(ExportarAlbarans.class).exportar(KeyArticleClient.of(artint, clicod), codiAlbaraFacturaReferenciaTransit, acumulatSegonsClient);
		return Response.ok(bytes)
				.header("Content-Disposition", "attachment;filename=conf_edi.xlsx")
				.build();
	}

	// TODO Canviar per PUT quan esigui integrat amb el frontend
	@GET
	@Path("/{codi}/{empresa}/aviexp")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public String generarAviexp(@PathParam("codi") Long codi, @PathParam("empresa") String empresa) {
		var idAlbara = KeyAlbara.of(codi, empresa);
		return BeanUtils.getBean(EnviarAviexp.class).executar(idAlbara);
	}

	@GET
	@Path("/{codi}/{empresa}/justificant")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generarAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa,
								  @QueryParam("idioma") @DefaultValue("en") String idioma,
								  @QueryParam("format_decimal") @DefaultValue("COMA_PUNT") TipusFormatDecimal formatDecimal,
								  @QueryParam("incloure_etiquetes") @DefaultValue("false") boolean incloureEtiquetes,
								  @QueryParam("is_copia") @DefaultValue("false") boolean isCopia) {
		var idAlbara = KeyAlbara.of(codi, empresa);
		var bytes = BeanUtils.getBean(GenerarPdfAlbara.class).run(idAlbara, idioma, formatDecimal, incloureEtiquetes, isCopia);
		var fileName = String.format("albara_%07d_%s", codi, empresa);
		return Response.ok(bytes)
				.type("application/pdf")
				.header("Content-Disposition", "filename=\"" + fileName + "\"")
				.build();
	}

	@GET
	@Path("/{codi}/{empresa}/certificat/{tipus}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generarCertificatAlbara(@PathParam("codi") Long codi, @PathParam("empresa") String empresa,
											@PathParam("tipus") String tipus,
											@QueryParam("idioma") @DefaultValue("en") String idioma) {
		var idAlbara = KeyAlbara.of(codi, empresa);
		var bytes = BeanUtils.getBean(GenerarPdfCertificatAlbara.class).run(idAlbara, tipus, idioma);
		var fileName = String.format("cert_%s_%07d_%s", tipus, codi, empresa);
		return Response.ok(bytes)
				.type("application/pdf")
				.header("Content-Disposition", "filename=\"" + fileName + "\"")
				.build();
	}

	@GET
	@Path("/{albara}/adjunts")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Adjunt> getAdjunts(@PathParam("albara") long albara) {
		return BeanUtils.getBean(ObtenirAdjuntsAlbara.class).executar(albara);
	}

	@POST
	@Path("/{albara}/adjunts")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void pujarAdjunt(@PathParam("albara") long albara, @FormDataParam("file") InputStream inputStream,
							@FormDataParam("file") FormDataContentDisposition fileDetail) {
		String nomFitxer = new String(fileDetail.getFileName().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		BeanUtils.getBean(AfegirAdjuntAlbara.class).executar(albara, inputStream, nomFitxer);
	}

	@PUT
	@Path("/{albara}/adjunts/{codiAdjunt}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void renombrarAdjunt(@PathParam("albara") long albara, @PathParam("codiAdjunt") String codiAdjunt, StringRequest req) {
		BeanUtils.getBean(RenombrarAdjuntAlbara.class).executar(albara, codiAdjunt, req.valor());
	}

	@DELETE
	@Path("/{albara}/adjunts/{codiAdjunt}")
	public void eliminarAdjunt(@PathParam("albara") long albara, @PathParam("codiAdjunt") String codiAdjunt) {
		BeanUtils.getBean(EliminarAdjuntAlbara.class).executar(albara, codiAdjunt);
	}

	@GET
	@Path("/{albara}/adjunts/{codiAdjunt}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response descarregarAdjunt(@PathParam("albara") long albara, @PathParam("codiAdjunt") String codiAdjunt) {
		var nomFile = BeanUtils.getBean(ObtenirAdjuntAlbara.class).executar(albara, codiAdjunt).nom();
		var file = BeanUtils.getBean(ObtenirAdjuntAlbaraFile.class).executar(albara, codiAdjunt);
		return Response.status(200)
				.entity(file)
				.header("Content-Disposition", "attachment; filename=" + nomFile)
				.build();
	}

	@PUT
	@Path("/{albara}/{empresa}/urgent")
	@Consumes(MediaType.APPLICATION_JSON)
	public void marcarAlbaraUrgent(@PathParam("albara") long albara, @PathParam("empresa") String empresa, MarcarAlbaraUrgentRequest req) {
		KeyAlbara idAlbara = KeyAlbara.of(albara, empresa);
		BeanUtils.getBean(MarcarAlbaraUrgent.class).executar(idAlbara, req.costEnviamentExpress(), req.isUrgent());
	}

	@PUT
	@Path("/{albara}/{empresa}/nourgent")
	@Consumes(MediaType.APPLICATION_JSON)
	public void desmarcarAlbaraUrgent(@PathParam("albara") long albara, @PathParam("empresa") String empresa) {
		KeyAlbara idAlbara = KeyAlbara.of(albara, empresa);
		BeanUtils.getBean(DesmarcarAlbaraUrgent.class).executar(idAlbara);
	}


	@PUT
	@Path("/{albara}/{empresa}/linies/{idLinia}/urgent")
	@Consumes(MediaType.APPLICATION_JSON)
	public void marcarLiniaUrgent(@PathParam("albara") long albara, @PathParam("empresa") String empresa, @PathParam("idLinia") long idLinia, BooleanRequest req) {
		KeyAlbara idAlbara = KeyAlbara.of(albara, empresa);
		BeanUtils.getBean(MarcarLiniaAlbaraUrgent.class).executar(idAlbara, idLinia, req.valor());
	}

	@GET
	@Path("/{albara}/{empresa}/urgent")
	@Consumes(MediaType.APPLICATION_JSON)
	public Albara obtenirAlbaraUrgent(@PathParam("albara") long albara, @PathParam("empresa") String empresa) {
		KeyAlbara idAlbara = KeyAlbara.of(albara, empresa);
		return BeanUtils.getBean(ObtenirAlbaraUrgent.class).executar(idAlbara);
	}

}

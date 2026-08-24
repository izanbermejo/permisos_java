package ames.comercial.inventari;

import ames.comercial.inventari.ext.IObtenirFitxesMagatzem;
import ames.comercial.inventari.ext.IObtenirFitxesMagatzem.ObtenirFitxesMagatzemResponse;
import ames.comercial.inventari.internal.application.command.AfegirFerralla;
import ames.comercial.inventari.internal.application.command.AfegirFerralla.AfegirFerrallaRequest;
import ames.comercial.inventari.internal.application.command.AfegirRegularitzacio;
import ames.comercial.inventari.internal.application.command.AfegirRegularitzacio.AfegirRegularitzacioRequest;
import ames.comercial.inventari.internal.application.command.EliminarFerralla;
import ames.comercial.inventari.internal.application.command.EliminarRegularitzacio;
import ames.comercial.inventari.internal.application.command.ModificarFerralla;
import ames.comercial.inventari.internal.application.command.ModificarFerralla.ModificarFerrallaRequest;
import ames.comercial.inventari.internal.application.command.ModificarRegularitzacio;
import ames.comercial.inventari.internal.application.command.ModificarRegularitzacio.ModificarRegularitzacioRequest;
import ames.comercial.inventari.internal.application.query.ObtenirHistoric;
import ames.comercial.inventari.internal.application.query.ObtenirHistoric.ObtenirHistoricResponse;
import ames.comercial.inventari.internal.application.query.ObtenirInformacioInventari;
import ames.comercial.inventari.internal.application.query.ObtenirInformacioInventari.ObtenirInformacioInventariResponse;
import ames.comercial.inventari.internal.application.query.ObtenirMoviments;
import ames.comercial.inventari.internal.application.query.ObtenirMoviments.ObtenirMovimentsResponse;
import ames.comercial.inventari.internal.application.query.ObtenirMovimentsRequestImpl;
import ames.comercial.inventari.internal.application.query.ObtenirMoviments.FiltreMoviment;
import ames.comercial.inventari.internal.application.query.ExportarFerralla;
import ames.comercial.inventari.internal.application.query.ExportarPecesImmobilitzades;
import ames.comercial.inventari.internal.application.query.ObtenirFerralla;
import ames.comercial.migracio.*;
import ames.comercial.server.BeanUtils;
import ames.comercial.shared.KeyArticleClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Path("/inventari")
public class EndPointInventari {

	// TODO Eliminar aquest endpoint quan s'hagi fet la migració
	@GET
	@Path("/migracioAll")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void migracioAll() {
		BeanUtils.getBean(MigracioCapsalera.class).migracio();
		BeanUtils.getBean(MigracioLiniesAlbara.class).migracio();
		BeanUtils.getBean(MigracioFitxes.class).migracio();
		BeanUtils.getBean(MigracioMoviments.class).migracio();
		BeanUtils.getBean(MigracioFacturacioLiniesAlbara.class).migracio();
	}

	// TODO Eliminar aquest endpoint quan s'hagi fet la migració
	@GET
	@Path("/migracioHis")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void migracio() {
		BeanUtils.getBean(MigracioMoviments.class).migracio();
	}

	// TODO Eliminar aquest endpoint quan s'hagi fet la migració
	@GET
	@Path("/migracioArtfit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void migracioArtfit() {
		BeanUtils.getBean(MigracioFitxes.class).migracio();
	}

	// TODO Eliminar aquest endpoint quan s'hagi fet la migració
	@GET
	@Path("/migracioAlbcap")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void migracioAlbcap() {
		BeanUtils.getBean(MigracioCapsalera.class).migracio();
	}

	// TODO Eliminar aquest endpoint quan s'hagi fet la migració
	@GET
	@Path("/migracioAlblin")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void migracioAlblin() {
		BeanUtils.getBean(MigracioLiniesAlbara.class).migracio();
	}

	// TODO Eliminar aquest endpoint quan s'hagi fet la migració
	@GET
	@Path("/migracioFacturacio")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void migracioLiniesFacturacio() {
		BeanUtils.getBean(MigracioFacturacioLiniesAlbara.class).migracio();
	}

	// TODO Eliminar aquest endpoint quan s'hagi fet la migració
	@GET
	@Path("/migracioMagatzems")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void migracioMagatzems() {
		BeanUtils.getBean(MigracioMagatzems.class).migracio();
	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObtenirMovimentsResponse obtenirMoviments(@QueryParam("artint") String artint,
											   @QueryParam("clicod") String clicod,
											   @QueryParam("empresa") String empresa,
											   @QueryParam("magatzem") String magatzem,
											   @QueryParam("dataInici") @DefaultValue("1900-01-01") LocalDate dataInici,
											   @QueryParam("dataFi") LocalDate dataFi,
											   @QueryParam("mostrarOrdreAscendent") @DefaultValue("true") boolean mostrarOrdreAscendent,
											   @QueryParam("filtresMoviment") String filtresMovimentStr) {
		List<FiltreMoviment> filtresMoviment = (filtresMovimentStr == null || filtresMovimentStr.isBlank())
				? List.of()
				: Arrays.stream(filtresMovimentStr.split(","))
						.map(s -> FiltreMoviment.valueOf(s.trim()))
						.toList();
		var request = ObtenirMovimentsRequestImpl.builder()
					.articleClient(KeyArticleClient.of(artint, clicod))
					.empresa(empresa)
					.magatzem(magatzem)
					.dataInici(dataInici)
					.dataFi(dataFi)
					.isOrdreAscendent(mostrarOrdreAscendent)
					.filtresMoviment(filtresMoviment)
					.build();
		return BeanUtils.getBean(ObtenirMoviments.class).executar(request);
	}

	@GET
	@Path("/historic/{artint}/{clicod}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObtenirHistoricResponse obtenirHistoric(@PathParam("artint") String artint,
												   @PathParam("clicod") String clicod,
												   @QueryParam("dataInici") LocalDate dataInici,
												   @QueryParam("dataFi") LocalDate dataFi,
												   @QueryParam("empresa") String empresa,
												   @QueryParam("magatzem") String magatzem,
												   @QueryParam("filtresMoviment") String filtresMovimentStr) {
		List<FiltreMoviment> filtresMoviment = (filtresMovimentStr == null || filtresMovimentStr.isBlank())
				? List.of()
				: Arrays.stream(filtresMovimentStr.split(","))
						.map(s -> FiltreMoviment.valueOf(s.trim()))
						.toList();
		return BeanUtils.getBean(ObtenirHistoric.class).executar(
				KeyArticleClient.of(artint, clicod), dataInici, dataFi,
				Optional.ofNullable(empresa).filter(s -> !s.isBlank()),
				Optional.ofNullable(magatzem).filter(s -> !s.isBlank()),
				filtresMoviment);
	}

	@GET
	@Path("/stocks/{artint}/{clicod}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ObtenirFitxesMagatzemResponse obtenirArtfitStocks(@PathParam("artint") String artint,
															 @PathParam("clicod") String clicod) {
		return BeanUtils.getBean(IObtenirFitxesMagatzem.class).executar(KeyArticleClient.of(artint, clicod));
	}

	/**
	 * Full de càlcul amb el llistat de peces immobilitzades: articles-client amb existències al
	 * final de {@code mes}, que ja en tenien 12 mesos abans i sense cap sortida pel mig.
	 */
	@GET
	@Path("/immobilitzats")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportarPecesImmobilitzades(@QueryParam("mes") String mes) throws IOException {
		var mesReferencia = (mes == null || mes.isBlank()) ? YearMonth.now() : YearMonth.parse(mes);
		var bytes = BeanUtils.getBean(ExportarPecesImmobilitzades.class).exportar(mesReferencia);
		var nomFitxer = String.format("immobilitzats_%s.xlsx", mesReferencia);
		return Response.ok(bytes)
				.header("Content-Disposition", "attachment;filename=" + nomFitxer)
				.build();
	}

	/**
	 * Full de càlcul amb el llistat de ferralla dels 12 mesos anteriors a {@code mes}: moviments de
	 * ferralla acumulats per article-client i valorats a preu de cost en euros.
	 */
	@GET
	@Path("/ferralla/llistat")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportarFerralla(@QueryParam("mes") String mes) throws IOException {
		var mesReferencia = (mes == null || mes.isBlank()) ? YearMonth.now() : YearMonth.parse(mes);
		var bytes = BeanUtils.getBean(ExportarFerralla.class).exportar(mesReferencia);
		var periode = ObtenirFerralla.periodeDe(mesReferencia);
		var nomFitxer = String.format("ferralla_%s_%s.xlsx",
				YearMonth.from(periode.dataInici()), YearMonth.from(periode.dataFi()));
		return Response.ok(bytes)
				.header("Content-Disposition", "attachment;filename=" + nomFitxer)
				.build();
	}

	@GET
	@Path("/informacio/{artint}/{clicod}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Optional<ObtenirInformacioInventariResponse> obtenirInformacioInventari(@PathParam("artint") String artint,
																				   @PathParam("clicod") String clicod) {
		return BeanUtils.getBean(ObtenirInformacioInventari.class).executar(KeyArticleClient.of(artint, clicod));
	}

	@POST
	@Path("/ferralla")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void afegirFerralla(AfegirFerrallaRequest request) {
		BeanUtils.getBean(AfegirFerralla.class).executar(request);
	}

	@PUT
	@Path("/ferralla/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void modificarFerralla(@PathParam("id") long id, ModificarFerrallaRequest request) {
		BeanUtils.getBean(ModificarFerralla.class).executar(id, request);
	}

	@DELETE
	@Path("/ferralla/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void eliminarFerralla(@PathParam("id") long id) {
		BeanUtils.getBean(EliminarFerralla.class).executar(id);
	}

	@POST
	@Path("/regularitzacio")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void afegirRegularitzacio(AfegirRegularitzacioRequest request) {
		BeanUtils.getBean(AfegirRegularitzacio.class).executar(request);
	}

	@PUT
	@Path("/regularitzacio/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void modificarRegularitzacio(@PathParam("id") long id, ModificarRegularitzacioRequest request) {
		BeanUtils.getBean(ModificarRegularitzacio.class).executar(id, request);
	}

	@DELETE
	@Path("/regularitzacio/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void eliminarRegularitzacio(@PathParam("id") long id) {
		BeanUtils.getBean(EliminarRegularitzacio.class).executar(id);
	}

}

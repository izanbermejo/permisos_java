package ames.comercial.propostes;

import ames.comercial.propostes.ObtenirPropostesClient.ObtenirPropostesClientRequest;
import ames.comercial.propostes.ObtenirPropostesMagatzem.ObtenirPropostesMagatzemRequest;
import ames.comercial.propostes.ObtenirResumPropostesEntrega.ObtenirResumPropostesEntregaRequest;
import ames.comercial.propostes.internal.application.query.ObtenirLiniesTraspasGrup;
import ames.comercial.propostes.internal.application.query.ObtenirLiniesTraspasGrup.LiniesTraspasGrupResponse;
import ames.comercial.propostes.internal.application.query.RecalcularImportPesLinia;
import ames.comercial.propostes.internal.application.query.RecalcularImportPesLinia.RecalculLiniaRequest;
import ames.comercial.propostes.internal.application.query.RecalcularImportPesLinia.RecalculLiniaResponse;
import ames.comercial.propostes.response.PropostesClientResponse;
import ames.comercial.propostes.response.PropostesTraspasResponse;
import ames.comercial.propostes.response.ResumPropostesEntregaResponse;
import ames.comercial.server.BeanUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.time.LocalDate;

@Path("/propostes")
public class EndPointPropostes {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ResumPropostesEntregaResponse obtenirResumPropostesEntrega(@BeanParam ObtenirResumPropostesEntregaRequest request) {
		return BeanUtils.getBean(ObtenirResumPropostesEntrega.class).executar(request);
	}

	@GET
	@Path("/client/{client}/{empresa}")
	@Produces(MediaType.APPLICATION_JSON)
	public PropostesClientResponse obtenirPropostesClient(@PathParam("client") String client,
														  @PathParam("empresa") String empresa,
														  @BeanParam ObtenirPropostesClientRequest request) {
		return BeanUtils.getBean(ObtenirPropostesClient.class).executar(client, empresa, request);
	}

	@GET
	@Path("/magatzem/{magatzem}")
	@Produces(MediaType.APPLICATION_JSON)
	public PropostesTraspasResponse obtenirPropostesMagatzem(@PathParam("magatzem") String magatzemSortida,
															 @BeanParam ObtenirPropostesMagatzemRequest request) {
		return BeanUtils.getBean(ObtenirPropostesMagatzem.class).executar(magatzemSortida, request);
	}

	@GET
	@Path("/traspas/linies")
	@Produces(MediaType.APPLICATION_JSON)
	public LiniesTraspasGrupResponse obtenirLiniesTraspasGrup(@QueryParam("artint") String artint,
														   @QueryParam("clicod") String clicod,
														   @QueryParam("client") String client,
														   @QueryParam("empresa") String empresa,
														   @QueryParam("magOrigen") String magOrigen,
														   @QueryParam("magDesti") String magDesti,
														   @QueryParam("dataPrevistaInici") @DefaultValue("1900-01-01") LocalDate dataPrevistaInici,
														   @QueryParam("dataPrevistaFi") @DefaultValue("2999-12-31") LocalDate dataPrevistaFi) {
		return BeanUtils.getBean(ObtenirLiniesTraspasGrup.class).executar(artint, clicod, client, empresa, magOrigen, magDesti, dataPrevistaInici, dataPrevistaFi);
	}

	@POST
	@Path("/linia/recalcul")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public RecalculLiniaResponse recalcularLinia(RecalculLiniaRequest request) {
		return BeanUtils.getBean(RecalcularImportPesLinia.class).executar(request);
	}

}

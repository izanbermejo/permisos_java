package ames.comercial.ofs;

import ames.comercial.ofs.internal.application.command.AnularOrdreFabricacio;
import ames.comercial.ofs.internal.application.query.ObtenirOF;
import ames.comercial.ofs.internal.application.query.ObtenirOFsPerArticleClient;
import ames.comercial.ofs.internal.application.query.ObtenirOFsPerArticleClient.OrdreFabricacioResponse;
import ames.comercial.ofs.internal.application.query.ObtenirUltimaOf;
import ames.comercial.ofs.response.ObtenirOFResponse;
import ames.comercial.server.BeanUtils;
import ames.comercial.shared.KeyArticleClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Path("/ofs")
public class EndPointOfs {

	@GET
	@Path("historic/{articleClient}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<OrdreFabricacioResponse> obtenirLlistatOF(@QueryParam("artInt") String artInt,
                                                          @QueryParam("cliCod") String cliCod){
		KeyArticleClient keyArticleClient = KeyArticleClient.of(artInt, cliCod);
		return BeanUtils.getBean(ObtenirOFsPerArticleClient.class).get(keyArticleClient);
	}

    @GET
    @Path("artcli/{artInt}/{cliCod}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Optional<ObtenirOFResponse> obtenirOFByArtCli(@PathParam("artInt") String artInt,
                                                         @PathParam("cliCod") String cliCod){
        KeyArticleClient keyArticleClient = KeyArticleClient.of(artInt, cliCod);
        return BeanUtils.getBean(ObtenirUltimaOf.class).get(keyArticleClient);
    }

    @GET
	@Path("{numOF}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Optional<ObtenirOFResponse> obtenirOFByNumOF(@PathParam("numOF") Long numOF) {
		return BeanUtils.getBean(ObtenirOF.class).get(numOF);
	}

    @PUT
    @Path("/{numOF}/anular")
    public void anularOF(@PathParam("numOF") long numOF) {
        BeanUtils.getBean(AnularOrdreFabricacio.class).executar(numOF);
    }

}
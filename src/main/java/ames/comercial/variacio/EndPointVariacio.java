package ames.comercial.variacio;

import ames.comercial.server.BeanUtils;
import ames.comercial.shared.Divisa;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;

@Path("/variacio")
public class EndPointVariacio {

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response variacio(@QueryParam("fabrica") List<String> fabriques,
							 @QueryParam("dataInici") LocalDate dataInici,
							 @QueryParam("dataFins") LocalDate dataFins,
							 @QueryParam("divisa") @DefaultValue("EUR") String divisaParam,
							 @QueryParam("dataSolicitadaInici") LocalDate dataSolicitadaInici,
							 @QueryParam("dataSolicitadaFi") LocalDate dataSolicitadaFi) {
		// Conversió de la divisa
		var divisa = Divisa.getBySymbol(divisaParam.toUpperCase());
		// Les dates sol·licitades poden ser NULL i es posen el valor minim a la data d'inici i el màxim a la de fi
		if (dataSolicitadaInici == null)
			dataSolicitadaInici = LocalDate.MIN;
		if (dataSolicitadaFi == null)
			dataSolicitadaFi = LocalDate.MAX;
		byte[] bytes = BeanUtils.getBean(GenerarReportVariacio.class).executar(dataInici, dataFins, fabriques, divisa,
				dataSolicitadaInici, dataSolicitadaFi);
		return Response.ok(bytes)
				.header("Content-Disposition", "attachment;filename=Variacio.xlsx")
				.build();
	}
	
}

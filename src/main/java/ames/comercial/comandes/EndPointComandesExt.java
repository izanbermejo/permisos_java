package ames.comercial.comandes;

import ames.comercial.comandes.internal.application.command.CarregarComandesMarketing;
import ames.comercial.comandes.internal.application.command.CarregarStocksSeguretat;
import ames.comercial.comandes.internal.application.command.DescarregaStockSeguretat;
import ames.comercial.server.BeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

@Path("ext/comanda")
public class EndPointComandesExt {

	static final Logger log = LogManager.getLogger(EndPointComandesExt.class.getName());

	private @Autowired ObjectMapper jsonMapper;

	@POST
	@Path("/stockseguretat/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void actualitzarStocksSeguretat (@FormDataParam("file") InputStream inputStream,
														 @FormDataParam("file") FormDataContentDisposition fileDetail,
														 @HeaderParam("params") String jsonParams) {
		BeanUtils.getBean(CarregarStocksSeguretat.class).executar(inputStream);
	}

    @GET
    @Path("/stockseguretat/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response exportarStocksSeguretat() throws IOException {
        byte[] bytes = BeanUtils.getBean(DescarregaStockSeguretat.class).executar();
        return Response.ok(bytes)
                .header("Content-Disposition", "attachment; filename=stock_seguretat.xlsx")
                .build();
    }

	@POST
	@Path("/comandesmarketing/")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void crearComandesMarketing (@FormDataParam("file") InputStream inputStream,
											@FormDataParam("file") FormDataContentDisposition fileDetail,
											@HeaderParam("params") String jsonParams) {
		BeanUtils.getBean(CarregarComandesMarketing.class). executar(inputStream);
	}
	
}

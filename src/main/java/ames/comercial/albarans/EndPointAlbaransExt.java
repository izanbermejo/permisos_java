package ames.comercial.albarans;

import ames.comercial.albarans.internal.application.command.*;
import ames.comercial.albarans.internal.application.command.FacturarLiniesAlbara.FacturarLiniesAlbaraItemRequest;
import ames.comercial.albarans.internal.domain.albara.KeyAlbara;
import ames.comercial.server.BeanUtils;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("ext/albara")
public class EndPointAlbaransExt {

	@PUT
	@Path("/facturar")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void facturarLiniesAlbara(List<FacturarLiniesAlbaraItemRequest> facturarLiniesAlbaraItemRequests) {
		BeanUtils.getBean(FacturarLiniesAlbara.class).executar(facturarLiniesAlbaraItemRequests);
	}

	@PUT
	@Path("/desferfacturar/{empresa}/{factura}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void desferFacturarLiniesAlbara(@PathParam("empresa") String empresa, @PathParam("factura") String codiFactura) {
		BeanUtils.getBean(DesferFacturarLiniesAlbara.class).executar(empresa, codiFactura);
	}

	@PUT
	@Path("/{empresa}/{albara}/entregar")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void entregarAlbara(@PathParam("empresa") String empresa, @PathParam("albara") long albara) {
		var clauAlbara = KeyAlbara.of(albara, empresa);
		BeanUtils.getBean(EntregarAlbara.class).executar(clauAlbara);
	}

	@PUT
	@Path("/{empresa}/{albara}/servir")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void servirAlbara(@PathParam("empresa") String empresa, @PathParam("albara") long albara) {
		var clauAlbara = KeyAlbara.of(albara, empresa);
		BeanUtils.getBean(ServirAlbara.class).executar(clauAlbara);
	}

	@PUT
	@Path("/{empresa}/{albara}/desferservir")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void desferServirAlbara(@PathParam("empresa") String empresa, @PathParam("albara") long albara) {
		var clauAlbara = KeyAlbara.of(albara, empresa);
		BeanUtils.getBean(DesferServirAlbara.class).executar(clauAlbara);
	}

	@PUT
	@Path("/{empresa}/{albara}/enservei")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void enServeiAlabra(@PathParam("empresa") String empresa, @PathParam("albara") long albara) {
		var clauAlbara = KeyAlbara.of(albara, empresa);
		BeanUtils.getBean(EnServeiAlbara.class).executar(clauAlbara);
	}

	@PUT
	@Path("/{empresa}/{albara}/enpreparacio")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void enPreparacioAlbara(@PathParam("empresa") String empresa, @PathParam("albara") long albara) {
		var clauAlbara = KeyAlbara.of(albara, empresa);
		BeanUtils.getBean(EnPreparacioAlbara.class).executar(clauAlbara);
	}

	@PUT
	@Path("/{empresa}/{albara}/desferenpreparacio")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void desferEnPreparacioAlbara(@PathParam("empresa") String empresa, @PathParam("albara") long albara) {
		var clauAlbara = KeyAlbara.of(albara, empresa);
		BeanUtils.getBean(DesferEnPreparacioAlbara.class).executar(clauAlbara);
	}

}

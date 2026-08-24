package ames.comercial.edi2;

import ames.comercial.edi2.internal.application.query.GuardarInformacioEDI2;
import ames.comercial.server.BeanUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

@Path("ext/edi")
public class EndPointEDIExt {

	@POST
	@Path("/comandes/info/{codiComanda}")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public void pujarPDF(@PathParam("codiComanda") String codiComanda) {
		BeanUtils.getBean(GuardarInformacioEDI2.class).executar(codiComanda);
	}
	
}

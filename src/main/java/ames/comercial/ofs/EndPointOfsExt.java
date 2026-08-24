package ames.comercial.ofs;

import ames.comercial.ofs.internal.task.actions.TascaEnviamentOfsAction;
import ames.comercial.ofs.internal.task.actions.TascaCalcularOfsEspecialsAction;
import ames.comercial.ofs.internal.task.actions.TascaCalcularOfsMedicalAction;
import ames.comercial.ofs.internal.task.actions.TascaCalcularOfsNormalitzatsAction;
import ames.comercial.server.BeanUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("ext/ofs")
public class EndPointOfsExt {

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void normalitzats() {
		BeanUtils.getBean(TascaCalcularOfsNormalitzatsAction.class).executar();
	}

	@GET
	@Path("especials")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void especials() {
		BeanUtils.getBean(TascaCalcularOfsEspecialsAction.class).executar();
	}

	@GET
	@Path("medical")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void medical() {
		BeanUtils.getBean(TascaCalcularOfsMedicalAction.class).executar();
	}

	@GET
	@Path("enviament")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public void enviament() {
		BeanUtils.getBean(TascaEnviamentOfsAction.class).executar();
	}

}
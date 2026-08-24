package ames.comercial.comandes.request;

import ames.comercial.comandes.service.request.LiniaNormalitzatReq;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.List;

@JsonDeserialize(builder = CrearComandaNormalitzatRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CrearComandaNormalitzatRequest {

	String codiClient();
	String comanda();
	LocalDate dataRecepcio();
	List<LiniaNormalitzatReq> linies();
	
}

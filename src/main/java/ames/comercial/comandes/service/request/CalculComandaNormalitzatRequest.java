package ames.comercial.comandes.service.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.util.List;

@JsonDeserialize(builder = CalculComandaNormalitzatRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CalculComandaNormalitzatRequest {

	String codiClient();
	List<LiniaNormalitzatReq> linies();
	
}

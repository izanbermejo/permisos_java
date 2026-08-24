package ames.comercial.comandes.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

@JsonDeserialize(builder = CrearComandaNormalitzatResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CrearComandaNormalitzatResponse {

	long comanda();
	boolean avisComandaSuperiorDistribuidorNacional();
	
}

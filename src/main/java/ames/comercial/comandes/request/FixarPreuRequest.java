package ames.comercial.comandes.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;

@JsonDeserialize(builder = FixarPreuRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface FixarPreuRequest {

	BigDecimal valor();
	
}

package ames.comercial.comandes.service.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;

@JsonDeserialize(builder = CalculCostTransportRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CalculCostTransportRequest {

	String codiClient();
	BigDecimal importTotal();
	BigDecimal pesTotal();
}

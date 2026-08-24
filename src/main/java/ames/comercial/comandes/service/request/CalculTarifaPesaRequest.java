package ames.comercial.comandes.service.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.util.List;

@JsonDeserialize(builder = CalculTarifaPesaRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface CalculTarifaPesaRequest {

	String codiClient();
	List<LiniaNormalitzatReq> linies();
	BigDecimal importTotalNet();
	BigDecimal importTotalBrut();
	BigDecimal pesTotal();
	BigDecimal incrementPesTransport();
}

package ames.comercial.advantage.internal.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = QueryAlbaransFacturesByArticleAndClientResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface QueryAlbaransFacturesByArticleAndClientResponse {
	String albara();
	Optional<String> factura();
	LocalDate dataAlbaraFactura();
	long quantitat();
	long transit();
	long acumulat();
	String incoterm();
	@Value.Default
	default Boolean mateixEnviamentDelClient() {
		return true;
	}
	Optional <String> servida();
	String codiTransportista();
}

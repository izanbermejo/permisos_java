package ames.comercial.comandes.request;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(builder = QuantitatRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface QuantitatRequest {

	long quantitat();

	static QuantitatRequest of (long quantitat) {
		return QuantitatRequestImpl.builder()
				.quantitat(quantitat)
				.build();
	}
	
}

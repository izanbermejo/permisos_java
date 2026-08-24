package ames.comercial.advantage.internal.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;

@JsonDeserialize(builder = QueryFlagsClientsEDIResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface QueryFlagsClientsEDIResponse {
	Boolean plataforma();
	Boolean comandesAnticipadesExtra();
	long quantitat();
	long acumulat();
}

package ames.comercial.advantage.internal.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = QueryAlbaransFacturesByComandesResponseImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface QueryAlbaransFacturesByComandesResponse {

	long comanda();
	long linia();
	String empresa();
	String albara();
	Optional<String> albaraEspecial();
	LocalDate dataAlbara();
	String enviamentAlbara();
	String factura();
	long quantitat();
	boolean entregat();
	
}

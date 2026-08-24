package ames.comercial.comandes.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;

@JsonDeserialize(builder = DefinirStockSeguretatRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface DefinirStockSeguretatRequest {

	LocalDate dataSolicitada();
	long quantitat();
	boolean stockSeguretatClient();

}

package ames.comercial.comandes.internal.domain.comanda;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;

@JsonDeserialize(builder = InformacioClientImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface InformacioClient {

	String identificador();
	LocalDate data();
	String programa();
	
}

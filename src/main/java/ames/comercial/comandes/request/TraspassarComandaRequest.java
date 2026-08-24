package ames.comercial.comandes.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.util.Optional;

@JsonDeserialize(builder = TraspassarComandaRequestImpl.Builder.class)
@Value.Style(typeImmutable = "*Impl")
@Value.Immutable
public interface TraspassarComandaRequest {

	/**
	 * @return Data a partir de la qual s'han de traspassar les línies
	 */
	Optional<LocalDate> data();
	// Dades de la nova comanda
	String comanda();
	String programa();

}
